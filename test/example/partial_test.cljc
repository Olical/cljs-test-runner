(ns example.partial-test
  (:require [#?(:clj clojure.test
                :cljs cljs.test) :as t]))

(t/deftest should-run
  (t/testing "this should run"
    (t/is (= 1 1))))
