(ns server.handlers
  (:require [server.constants :refer :all]
            [maze.core :as maze]
            [maze.spec :as maze-spec]
            [clojure.spec.alpha :as s]
            [cheshire.core :refer :all]
            [monger.collection :as mc])
  (:import  [com.mongodb MongoOptions ServerAddress]
            org.bson.types.ObjectId))

(defn post-maze
  "Generates a maze with the given request body parameters"
  [database request]
  (->
    (if (s/valid? ::maze-spec/maze-config (:body request))
      (let [maze (maze/gen-maze (:body request))
            id (ObjectId.)]
        ;; save the maze document to the mongodb collection
        (mc/insert database maze-collection-name (merge {:_id id} maze))
        ;; return the generated maze along with its id
        (assoc maze :_id (str id)))
      {:error "invalid maze configuration"
       :clojure-spec-explanation (s/explain-str ::maze-spec/maze-config (:body request))})
    (generate-string)))

(defn get-mazes
  "Returns all mazes"
  [database]
  (let [mazes (map #(assoc % :_id (str (:_id %))) (mc/find-maps database maze-collection-name))]
    (generate-string {:mazes mazes})))

(defn get-maze
  "Returns a single maze"
  [database id]
  (try
    (let [object-id (ObjectId. id)
          maze (mc/find-one-as-map database maze-collection-name {:_id object-id})]
      (generate-string {:maze (assoc maze :_id (str (:_id maze)))}))
    (catch Exception e
      (generate-string {:error "something went wrong"}))))