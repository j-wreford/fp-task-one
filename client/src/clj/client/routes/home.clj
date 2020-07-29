(ns client.routes.home
  (:require [solver.core :as solver]
            [clj-http.client :as client]
            [client.layout :as layout]
            [clojure.java.io :as io]
            [client.middleware :as middleware]
            [ring.util.response]
            [ring.util.http-response :as response]
            [cheshire.core :refer [parse-string]]))

(defn home-page [request]
  (prn request)
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn all-mazes-page [request]
  (let [client (client/get (str "http://127.0.0.1:5000/maze"))]
    (if (= (:status client) 200)
      (layout/render request "all-mazes.html" (parse-string (:body client)))
      (layout/render request "all-mazes.html" {:mazes false}))))

(defn single-maze-page [request]
  (let [maze-id (get-in request [:path-params :id])
        client (client/get (str "http://127.0.0.1:5000/maze/" maze-id))
        maze (:maze (parse-string (:body client) true))
        path (solver/solve-maze maze "dijkstra")]
    (if (= (:status client) 200)
      (layout/render request "single-maze.html" (assoc {:maze maze} :route path))
      (layout/render request "single-maze.html" {:maze false}))))

(defn gen-page [request]
  (layout/render request "generate.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get all-mazes-page}]
   ["/maze/:id" {:get single-maze-page}]
   ["/gen" {:get gen-page}]])