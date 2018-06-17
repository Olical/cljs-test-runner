(ns cljs-test-runner.main
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [cljs.build.api :as cljs]
            [doo.core :as doo]))

(def ns-filter-cljs
  "Namespace filtering code from cognitect-labs/test-runner but modified and as a string. Inserted into the test runner ClojureScript code when it's rendered. Forgive me performing string based meta programming."
  "
  (defn var->sym [var]
    (symbol (:ns (meta var)) (:name (meta var))))

  (defn var-filter
    [{:keys [var include exclude]}]
    (let [test-specific (if var
                          (comp var var->sym)
                          (constantly true))
          test-inclusion (if include
                           #((apply some-fn include) (meta %))
                           (constantly true))
          test-exclusion (if exclude
                           #((complement (apply some-fn exclude)) (meta %))
                           (constantly true))]
      #(and (test-specific %)
            (test-inclusion %)
            (test-exclusion %))))

  (defn unmap [ns sym]
    (js-delete ns (str (munge sym))))

  (defn filter-vars! [ns-syms filter-fn]
    (doseq [[ns syms] ns-syms]
      (doseq [[name var] syms]
        (when (:test (meta var))
          (when (not (filter-fn var))
            (unmap ns name))))))
  ")

(defn format-value
  "Return a string with a quote at the front. For use in this silly CLJS meta programming."
  [s]
  (str (when (symbol? s) "'") s))

(defn format-filter
  "Format filter values as a possible set or nil."
  [coll]
  (if coll
    (str "#{" (str/join " " (map format-value coll)) "}")
    "nil"))

(defn render-test-runner-cljs
  "Renders a ClojureScript test runner from a seq of namespaces."
  [nses {:keys [var include exclude]}]
  (str
    "(ns test.runner
       (:require [doo.runner :refer-macros [doo-tests]]"
                 (str/join " " nses)"))"
     ns-filter-cljs
     "(filter-vars! {" (str/join ", " (map #(str % " (ns-publics " (format-value %) ")") nses)) "}
        (var-filter {:var " (format-filter var) "
                     :include " (format-filter include) "
                     :exclude " (format-filter exclude) "}))"
     "(doo-tests " (str/join " " (map format-value nses)) ")"))

(defn ns-filter-fn
  "Given possible namespace symbols and regexs, return a function that returns true if it's given namespace matches one of the rules."
  [{:keys [ns-symbols ns-regexs]}]
  (let [ns-regexs (or ns-regexs #{#".*\-test$"})]
    (fn [n]
      (if ns-symbols
        (ns-symbols n)
        (some #(re-matches % (name n)) ns-regexs)))))

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

(defn find-namespaces-in-dirs [dirs]
  (mapcat #(find/find-namespaces-in-dir (io/file %) find/cljs) dirs))

(defn test-cljs-namespaces-in-dir
  "Execute all ClojureScript tests in a directory."
  [{:keys [env dir out watch ns-symbols ns-regexs var include exclude]}]
  (let [test-runner-cljs (-> (find-namespaces-in-dirs dir)
                             (->> (filter (ns-filter-fn {:ns-symbols ns-symbols
                                                         :ns-regexs ns-regexs})))
                             (render-test-runner-cljs {:var var
                                                       :include include
                                                       :exclude exclude}))
        exit-code (atom 1)
        src-path (str/join "/" [(first dir) "cljs-test-runner.temp.cljs"])
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
          (do (cljs/build (first dir) build-opts)
              (->> (run-tests-fn) :exit (reset! exit-code)))))
      (catch Exception e
        (println e))
      (finally
        (exit @exit-code)))))

(defn parse-kw
  "Parse a keyword from a string, dropping the initial : if required."
  [s]
  (if (.startsWith s ":") (read-string s) (keyword s)))

(defn accumulate
  "Used in CLI options to accumulate multiple occurrences of a flag."
  [m k v]
  (update-in m [k] (fnil conj #{}) v))

(def cli-options
  [["-d" "--dir DIRNAME" "The directory containing your test files"
    :default #{"test"}
    :assoc-fn accumulate]
   ["-n" "--namespace SYMBOL" "Symbol indicating a specific namespace to test."
    :id :ns-symbols
    :parse-fn symbol
    :assoc-fn accumulate]
   ["-r" "--namespace-regex REGEX" "Regex for namespaces to test. Only namespaces ending in '-test' are evaluated by default."
    :id :ns-regexs
    :default-desc ".*\\-test$"
    :parse-fn re-pattern
    :assoc-fn accumulate]
   ["-v" "--var SYMBOL" "Symbol indicating the fully qualified name of a specific test."
    :parse-fn symbol
    :assoc-fn accumulate]
   ["-i" "--include SYMBOL" "Run only tests that have this metadata keyword."
    :parse-fn parse-kw
    :assoc-fn accumulate]
   ["-e" "--exclude SYMBOL" "Exclude tests with this metadata keyword."
    :parse-fn parse-kw
    :assoc-fn accumulate]
   ["-o" "--out DIRNAME" "The output directory for compiled test code"
    :default "cljs-test-runner-out"]
   ["-x" "--env ENV" "Run your tests in either node or phantom."
    :default :node
    :default-desc "node"
    :parse-fn keyword]
   ["-w" "--watch DIRNAME" "Directory to watch for changes (alongside the test directory). May be repeated."
    :assoc-fn accumulate]
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
  ;; all
  (with-redefs [exit println] (-main))

  ;; ns symbol
  (with-redefs [exit println] (-main "-n" "example.yes-test"))

  ;; ns regexs
  (with-redefs [exit println] (-main "-r" ".*yes.*"))

  ;; var symbol
  (with-redefs [exit println] (-main "-v" "example.yes-test/should-run"))

  ;; include
  (with-redefs [exit println] (-main "-i" "integration"))

  ;; exclude
  (with-redefs [exit println] (-main "-e" "integration")))
