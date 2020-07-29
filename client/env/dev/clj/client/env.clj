(ns client.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [client.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[client started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[client has shut down successfully]=-"))
   :middleware wrap-dev})
