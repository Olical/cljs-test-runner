(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [cljs.build.api :as cljs]))

(defn render-test-runner-cljs
  "Renders a ClojureScript test runner from a seq of namespaces."
  [nses]
  (let [nses-str (str/join " " nses)
        quoted-nses-str (str/join " " (map #(str "'" %) nses))]
    (str "(ns test.runner (:require cljs-testrunners.node " nses-str ") ) (cljs-testrunners.node/run-tests " quoted-nses-str ")")))

;; todo
;; use doo for execution, support multiple envs
;; build into a temp dir, or just not out by default

(defn -main
  "Creates a ClojureScript test runner and executes it with node."
  [& args]
  (let [test-runner-cljs (-> (io/file "test")
                             (find/find-namespaces-in-dir find/cljs)
                             (render-test-runner-cljs))
        src-path "test/runner.cljs"
        out-path "out/test-runner.js"]
    (spit src-path test-runner-cljs)
    (try
      (cljs/build "test" {:output-to out-path
                          :main 'test.runner
                          :target :nodejs})
      (let [{:keys [out err exit]} (shell/sh "node" out-path)]
        (println out err)
        (io/delete-file src-path)
        (System/exit exit))
      (catch Error e
        (io/delete-file src-path)
        (println e)))))
