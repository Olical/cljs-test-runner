(ns user
  (:require [cemerick.piggieback :as pback]
            [cljs.repl.node :as node]))

(defn cljs-repl []
  (pback/cljs-repl (node/repl-env)))
