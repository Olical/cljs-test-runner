(ns cljs-test-runner.nrepl
  (:require [cider-nrepl.main :as nrepl]
            [cemerick.piggieback :as pback]))

(defn -main []
  (cider-nrepl.main/init ["cider.nrepl/cider-middleware"
                          "cemerick.piggieback/wrap-cljs-repl"]))
