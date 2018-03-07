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

(defn test-namespace?
  "Checks if a namespace symbol is a test namespace (ends with -test) or not."
  [ns-name]
  (str/ends-with? ns-name "-test"))

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

(defn test-cljs-namespaces-in-dir
  "Execute all ClojureScript tests in a directory."
  [{:keys [env src out]}]
  (let [test-runner-cljs (-> (io/file src)
                             (find/find-namespaces-in-dir find/cljs)
                             (->> (filter test-namespace?))
                             (render-test-runner-cljs))
        exit-code (atom 1)
        src-path (str/join "/" [src "runner.cljs"])
        out-path (str/join "/" [out "test-runner.js"])
        {:keys [target doo-env]} (case env
                                   :node {:target :nodejs
                                          :doo-env :node}
                                   :phantom {:target :browser
                                             :doo-env :phantom})]
    (spit src-path test-runner-cljs)
    (try
      (let [doo-opts {}
            compiler-opts {:output-to out-path
                           :output-dir out
                           :target target
                           :main 'test.runner
                           :optimizations :none}]
        (cljs/build src compiler-opts)
        (let [{:keys [exit]} (doo/run-script doo-env compiler-opts doo-opts)]
          (reset! exit-code exit)))
      (finally
        (io/delete-file src-path)
        (exit @exit-code)))))

(def cli-options
  [["-e" "--env ENV" "Run your tests in either node or phantom"
    :default :node
    :default-desc "node"
    :parse-fn keyword]
   ["-s" "--src PATH" "The directory containing your test files"
    :default "./test"]
   ["-o" "--out PATH" "The output directory for compiled test code"
    :default "./cljs-test-runner-out"]
   ["-h" "--help"]])

(defn -main
  "Creates a ClojureScript test runner and executes it with node."
  [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (error-msg errors))
      :else (test-cljs-namespaces-in-dir options))))

