(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cljs.build.api :as cljs]
            [doo.core :as doo]))

(defn render-test-runner-cljs
  "Renders a ClojureScript test runner from a seq of namespaces."
  [nses]
  (let [nses-str (str/join " " nses)
        quoted-nses-str (str/join " " (map #(str "'" %) nses))]
    (str "(ns test.runner (:require [doo.runner :refer-macros [doo-tests]] " nses-str ") ) (doo-tests " quoted-nses-str ")")))

(defn -main
  "Creates a ClojureScript test runner and executes it with node."
  [& args]
  (let [test-runner-cljs (-> (io/file "test")
                             (find/find-namespaces-in-dir find/cljs)
                             (render-test-runner-cljs))
        exit-code (atom 1)
        src-path "test/runner.cljs"
        out-dir "cljs-test-runner-out"
        out-path (str out-dir "/test-runner.js")]
    (spit src-path test-runner-cljs)
    (try
      (let [doo-opts {}
            compiler-opts {:output-to out-path
                           :output-dir out-dir
                           :main 'test.runner
                           :target :nodejs}]
        (cljs/build "test" compiler-opts)
        (let [{:keys [exit]} (doo/run-script :node compiler-opts doo-opts)]
          (reset! exit-code exit)))
      (catch Error e
        (println e))
      (finally
        (io/delete-file src-path)
        (System/exit @exit-code)))))

