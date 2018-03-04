(ns nrepl-server
  (:require [cider-nrepl.main :as nrepl]
            [cemerick.piggieback]))

(defn -main []
  (nrepl/init ["cider.nrepl/cider-middleware"
               "cemerick.piggieback/wrap-cljs-repl"]))
