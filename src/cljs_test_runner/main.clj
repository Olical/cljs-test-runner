(ns cljs-test-runner.main
  "Discover and run ClojureScript tests in node (by default)."
  (:require [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.tools.cli :as cli]
            [cljs.build.api :as cljs]
            [doo.core :as doo])
  (:import (clojure.lang DynamicClassLoader)))

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

  (defn filter-vars! [ns-syms filter-fn]
    (doseq [ns-sym ns-syms]
      (doseq [[_ var] ns-sym]
        (when (:test (meta var))
          (when (not (filter-fn var))
            (set! (.-cljs$lang$test @var) nil))))))
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
   "(ns cljs-test-runner.gen
       (:require [doo.runner :refer-macros [doo-tests]] [" (str/join "] [" nses) "]))"
   ns-filter-cljs
   "(filter-vars! [" (str/join " " (map #(str "(ns-publics " (format-value %) ")") nses)) "]
        (var-filter {:var " (format-filter var) "
                     :include " (format-filter include) "
                     :exclude " (format-filter exclude) "}))"
   "\n"
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

(defn find-namespaces-in-dirs
  "Given a set of directory paths, find every ClojureScript namespace within those directories and return it as one sequence."
  [dirs]
  (mapcat #(find/find-namespaces-in-dir (io/file %) find/cljs) dirs))

(defn load-opts
  "Load compiler options from input. Supports either an inline EDN map or assumed
  to be a file path. If input is nil, returns empty map"
  [path-or-data]
  (cond
    (nil? path-or-data)
    {}

    (= (first (str/trim path-or-data)) \{)
    (edn/read-string path-or-data)

    :else
    (edn/read-string (slurp path-or-data))))

(defn add-loader-url
  "Add url string or URL to the highest level DynamicClassLoader url set."
  [url]
  (let [u (if (string? url) (java.net.URL. url) url)
        loader (loop [loader (.getContextClassLoader (Thread/currentThread))]
                 (let [parent (.getParent loader)]
                   (if (instance? DynamicClassLoader parent)
                     (recur parent)
                     loader)))]
    (if (instance? DynamicClassLoader loader)
      (.addURL ^DynamicClassLoader loader u)
      (throw (IllegalAccessError. "Context classloader is not a DynamicClassLoader")))))

(defn test-cljs-namespaces-in-dir
  "Execute all ClojureScript tests in a directory."
  [{:keys [env dir out watch ns-symbols ns-regexs var include exclude verbose compile-opts doo-opts]}]
  (when-let [nses (seq (filter (ns-filter-fn {:ns-symbols ns-symbols
                                              :ns-regexs ns-regexs})
                               (find-namespaces-in-dirs dir)))]
    (let [test-runner-cljs (-> nses
                               (render-test-runner-cljs {:var var
                                                         :include include
                                                         :exclude exclude}))
          exit-code (atom 1)
          gen-path (str/join "/" [out "gen"])
          src-path (str/join "/" [gen-path "cljs_test_runner" "gen.cljs"])
          out-path (str/join "/" [out "cljs_test_runner.gen.js"])
          {:keys [target doo-env]} (case env
                                     :node {:target :nodejs
                                            :doo-env :node}
                                     :phantom {:target :browser
                                               :doo-env :phantom}
                                     :chrome-headless {:target :browser
                                                       :doo-env :chrome-headless}
                                     :firefox-headless {:target :browser
                                                        :doo-env :firefox-headless}
                                     :lumo {:doo-env :lumo}
                                     :planck {:doo-env :planck})]
      (io/make-parents src-path)
      (spit src-path test-runner-cljs)
      (add-loader-url (io/as-url (io/file gen-path)))
      (try
        (let [build-opts (merge {:output-to out-path
                                 :output-dir out
                                 :target target
                                 :main "cljs-test-runner.gen"
                                 :optimizations :none
                                 :verbose verbose}
                                compile-opts)
              run-tests-fn #(doo/run-script doo-env build-opts doo-opts)
              watch-opts (assoc build-opts :watch-fn run-tests-fn)]
          (if (contains? #{:lumo :planck} env)
            (->> (run-tests-fn) :exit (reset! exit-code))
            (if (seq watch)
              (cljs/watch (apply cljs/inputs (into watch (cons gen-path dir))) watch-opts)
              (do (cljs/build gen-path build-opts)
                  (->> (run-tests-fn) :exit (reset! exit-code))))))
        (catch Exception e
          (println e))
        (finally
          (exit @exit-code))))))

(defn parse-kw
  "Parse a keyword from a string, dropping the initial : if required."
  [s]
  (if (.startsWith s ":") (read-string s) (keyword s)))

(defn accumulate
  "Used in CLI options to accumulate multiple occurrences of a flag."
  [m k v]
  (update-in m [k] (fnil conj #{}) v))

(def cli-options
  "Options for use with clojure.tools.cli."
  [["-d" "--dir DIRNAME" "The directory containing your test files"
    :default-desc "test"
    :assoc-fn accumulate
    :default-fn (constantly #{"test"})]
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
   ["-x" "--env ENV" "Run your tests in node, phantom, chrome-headless, lumo or planck."
    :default :node
    :default-desc "node"
    :parse-fn parse-kw]
   ["-w" "--watch DIRNAME" "Directory to watch for changes (alongside the test directory). May be repeated."
    :assoc-fn accumulate]
   ["-c" "--compile-opts PATH" "EDN opts or EDN file containing opts to be passed to the ClojureScript compiler."
    :parse-fn load-opts]
   ["-D" "--doo-opts PATH" "EDN file containing opts to be passed to doo."
    :parse-fn load-opts]
   ["-V" "--verbose" "Flag passed directly to the ClojureScript compiler to enable verbose compiler output."]
   ["-H" "--help"]])

(defn -main
  "Creates a ClojureScript test runner and executes it with node (by default)."
  [& args]
  (let [cl (.getContextClassLoader (Thread/currentThread))]
    (.setContextClassLoader (Thread/currentThread) (clojure.lang.DynamicClassLoader. cl)))
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (error-msg errors))
      :else (test-cljs-namespaces-in-dir options))))

(comment
  (defn run
    "Runs the test suite with the give arguments without letting the process die at the end."
    [& args]
    (with-redefs [exit println]
      (println
       (with-out-str
         (apply -main args)))))

  ;; all
  (run)

  ;; ns symbol
  (run "-n" "example.yes-test")

  ;; ns regexs
  (run "-r" ".*yes.*")

  ;; var symbol
  (run "-v" "example.yes-test/should-run")

  ;; include
  (run "-i" "integration")

  ;; exclude
  (run "-e" "integration")

  ;; more dirs
  (run "-d" "other-tests")

  ;; doo-opts
  (run "-D" "test/doo-opts-test.edn")

  ;; cljs-opts
  (run "-c" "test/cljs-opts-test.edn")

  ;; help
  (run "-H"))
