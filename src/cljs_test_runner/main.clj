(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [cljs.build.api :as cljs]))

(defn render-test-src [nses]
  (str "(ns test.runner
  (:require cljs.test " (str/join " " nses) "))
(enable-console-print!)
(cljs.test/run-tests " (str/join " " (map #(str "'" %) nses))")"))

(defn -main [& args]
  (let [nses (->> (find/find-namespaces-in-dir (io/file "test") find/cljs)
                  (remove #{'test.runner}))]
    (spit "test/runner.cljs" (render-test-src nses))
    (try
      (cljs/build "test" {:output-to "out/test-runner.js"
                          :main 'test.runner
                          :target :nodejs})
      (let [{:keys [out err]} (shell/sh "node" "./out/test-runner.js")]
        (println out err))
      (shutdown-agents)
      (catch Exception e
        (println "Something went wrong in cljs-test-runner :C")
        (println e))
      (finally
        (io/delete-file "test/runner.cljs")))))
