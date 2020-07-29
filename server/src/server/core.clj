(ns server.core
  (:require [server.middleware :as mw]
            [server.handlers :as handlers]
            [server.constants :refer :all]
            [monger.core :as mg]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer :all]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.route :as route]))

; setup Monger connection to MongoDB
(def mg-connection (mg/connect))

; setup a handle to the maze collection using the established connection
(def database (mg/get-db mg-connection maze-db-name))

; map api routes to handler functions
(defroutes api-routes
           (POST "/maze" request
             (handlers/post-maze database request))
           (GET "/maze" []
             (handlers/get-mazes database))
           (GET "/maze/:id" [id]
             (handlers/get-maze database id)))

; main function
(defn -main []
  (jetty/run-jetty
    (-> api-routes
        (wrap-params api-defaults)
        (mw/wrap-content-type "application/json")
        (wrap-json-body {:keywords? true}))
    {:port 5000}))