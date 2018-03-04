(ns user
  (:require [cemerick.piggieback :as piggieback]
            [cljs.repl.node :as node-repl]))

(defn cljs-repl []
  (piggieback/cljs-repl (node-repl/repl-env)))
