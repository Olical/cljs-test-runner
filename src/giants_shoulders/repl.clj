(ns giants-shoulders.repl
  (:require [nrepl.server :as nrepl]
            [cider.nrepl :as cider]
            [taoensso.timbre :as log]
            [portal.api :as portal]
            [rebel-readline.core :as rr]
            [rebel-readline.clojure.line-reader :as rr-clr]
            [rebel-readline.clojure.service.local :as rr-csl]
            [rebel-readline.clojure.main :as rr-cm]
            [clojure.main :as clj-main]))

(defn start!
  "Start a development REPL, intended to be invoked from ./scripts/repl"
  [{:keys [portal]}]

  (log/info "Starting nREPL server")
  (let [{:keys [port] :as _server} (nrepl/start-server :handler cider/cider-nrepl-handler)]
    (log/info "nREPL server started on port" port)
    (log/info "Writing port to .nrepl-port")
    (spit ".nrepl-port" port))

  (when portal
    (log/info "Opening portal, use (tap> ...) to inspect values")
    (portal/open)
    (add-tap #'portal/submit))

  (log/info "Starting interactive REPL")
  (rr/with-line-reader
    (rr-clr/create (rr-csl/create))
    (clj-main/repl
     :prompt (fn [])
     :read (rr-cm/create-repl-read)))

  (log/info "Shutting down")

  (when portal
    (log/info "Closing portal")
    (portal/close))

  (shutdown-agents)
  (System/exit 0))
