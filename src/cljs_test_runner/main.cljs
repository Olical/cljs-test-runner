(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.parse :as parse]
            [cljs.tools.reader.reader-types :as reader-types]
            [cljs-node-io.core :as io]
            [cljs-node-io.fs :as fs]
            [cljs.test :as t]))

(do
  (defn path-kind [path]
    (cond
      (fs/file? path) :file
      (fs/dir? path) :dir))

  (defn ls [path]
    (map #(str path "/" %) (fs/readdir path)))

  (defn tree [path]
    (loop [[dir & dirs] [path]
           files []]
      (if (nil? dir)
        files
        (let [{:keys [file dir]} (group-by path-kind (ls dir))]
          (recur (concat dirs dir) (concat files file))))))

  (defn path->namespace-name [path]
    (-> (io/slurp path)
        (reader-types/string-push-back-reader)
        (parse/read-ns-decl parse/cljs-read-opts)
        (parse/name-from-ns-decl)))

  (defn cljs-path? [path]
    (boolean (re-find #"\.clj(s|c)$" path)))

  (defn ns-tree [path]
    (->> (tree path)
         (filter cljs-path?)
         (map path->namespace-name)))

  (let [nses (ns-tree "test")]
    (dorun (map require nses))
    (t/run-tests nses)))

(defn -main []
  (println "WIP"))
