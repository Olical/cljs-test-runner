(ns nrepl-server
  (:require [cider-nrepl.main :as nrepl]))

(defn -main []
  (nrepl/init ["cider.nrepl/cider-middleware"]))
