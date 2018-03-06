(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [cljs.main :as cljs]))

(defn render-test-runner-cljs
  "Renders a ClojureScript test runner from a seq of namespaces."
  [nses]
  (let [nses-str (str/join " " (map #(str "'" %) nses))]
    (str "(require 'cljs.test " nses-str ") (enable-console-print!) (cljs.test/run-tests " nses-str ")")))

(defn -main
  "Creates a ClojureScript test runner and runs it through cljs.main. All
  arguments passed to this are passed through to cljs.main apart from -e, this
  is used to execute the tests."
  [& args]
  (let [test-runner-cljs (-> (io/file "test")
                             (find/find-namespaces-in-dir find/cljs)
                             (render-test-runner-cljs))]
    (apply cljs/-main (concat args ["-e" test-runner-cljs]))))
