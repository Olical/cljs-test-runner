(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.parse :as parse]
            [cljs-node-io.core :as io]))

(do
  (defn find-namespaces-in-dir [dir]
    dir)

  (find-namespaces-in-dir "test"))

(defn -main []
  (println "WIP"))
