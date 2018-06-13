(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [cljs.build.api :as cljs]
            [doo.core :as doo]))

(defn render-test-runner-cljs
  "Renders a ClojureScript test runner from a seq of namespaces."
  [nses]
  (let [nses-str (str/join " " nses)
        quoted-nses-str (str/join " " (map #(str "'" %) nses))]
    (str "(ns test.runner (:require [doo.runner :refer-macros [doo-tests]] " nses-str ") ) (doo-tests " quoted-nses-str ")")))

(defn ns-filter-fn
  "Given a possible namespace symbol and regex, return a function that returns true if it's given namespace matches one of the rules."
  [ns-symbol ns-regex]
  (fn [n]
    (cond
      ns-symbol (= ns-symbol n)
      ns-regex (re-matches ns-regex (name n)))))

(defn exit
  "Exit the program cleanly."
  ([status]
   (exit status nil))
  ([status msg]
   (when msg
     (println msg))
   (System/exit status)))

(defn error-msg
  "Render an error message."
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn shutdown-hook
  "Add a function to be called when the JVM shuts down."
  [f]
  (let [shutdown-thread (new Thread f)]
    (.. Runtime (getRuntime) (addShutdownHook shutdown-thread))))

(defn test-cljs-namespaces-in-dir
  "Execute all ClojureScript tests in a directory."
  [{:keys [env dir out watch ns-symbol ns-regex]}]
  (let [test-runner-cljs (-> (io/file dir)
                             (find/find-namespaces-in-dir find/cljs)
                             (->> (filter (ns-filter-fn ns-symbol ns-regex)))
                             (render-test-runner-cljs))
        exit-code (atom 1)
        src-path (str/join "/" [dir "cljs-test-runner.temp.cljs"])
        out-path (str/join "/" [out "test-runner.js"])
        {:keys [target doo-env]} (case env
                                   :node {:target :nodejs
                                          :doo-env :node}
                                   :phantom {:target :browser
                                             :doo-env :phantom})]
    (spit src-path test-runner-cljs)
    (shutdown-hook #(io/delete-file src-path))
    (try
      (let [doo-opts {}
            build-opts {:output-to out-path
                        :output-dir out
                        :target target
                        :main 'test.runner
                        :optimizations :none}
            run-tests-fn #(doo/run-script doo-env build-opts doo-opts)
            watch-opts (assoc build-opts :watch-fn run-tests-fn)]
        (if (seq watch)
          (cljs/watch (apply cljs/inputs watch) watch-opts)
          (do (cljs/build dir build-opts)
              (->> (run-tests-fn) :exit (reset! exit-code)))))
      (catch Exception e
        (println e))
      (finally
        (exit @exit-code)))))

(def cli-options
  [["-x" "--env ENV" "Run your tests in either node or phantom."
    :default :node
    :default-desc "node"
    :parse-fn keyword]
   ["-n" "--namespace SYMBOL" "Symbol indicating a specific namespace to test."
    :id :ns-symbol
    :parse-fn symbol]
   ["-r" "--namespace-regex REGEX" "Regex for namespaces to test. Only namespaces ending in '-test' are evaluated by default."
    :id :ns-regex
    :default-desc ".*-test$"
    :default #".*-test$"
    :parse-fn re-pattern]
   ["-d" "--dir DIRNAME" "The directory containing your test files"
    :default "test"]
   ["-o" "--out DIRNAME" "The output directory for compiled test code"
    :default "cljs-test-runner-out"]
   ["-w" "--watch DIRNAME" "Directory to watch for changes (alongside the test directory). May be repeated."
    :assoc-fn (fn [m k v] (update m k (fnil conj [:dir]) v))]
   ["-h" "--help"]])

(defn -main
  "Creates a ClojureScript test runner and executes it with node (by default)."
  [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)
        options (update options :watch (partial replace options))]
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (error-msg errors))
      :else (test-cljs-namespaces-in-dir options))))

(comment
  (with-redefs [exit println]
    (-main)))
