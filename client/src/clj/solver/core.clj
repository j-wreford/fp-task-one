(ns solver.core
  (:require [maze.spec :as maze-spec]
            [solver.spec :as solver-spec]
            [clojure.data.priority-map :refer [priority-map priority-map-keyfn]]
            [clojure.set :refer :all]
            [clojure.spec.alpha :as s]))

(defn passages->graph
  "Creates a graph based off the given passages vector"
  [passages]
  {:pre  [(s/valid? ::maze-spec/passages passages)]
   :post [(s/valid? ::solver-spec/graph %)]}
  (reduce
    (fn [graph [a b]]
      (merge-with union graph {a #{b} b #{a}}))
    {} passages))

(defn gen-priority-map
  "Generates a priority map from the given graph"
  ([]
   (priority-map-keyfn #(:distance %)))
  ([graph]
   {:pre  [(s/valid? ::solver-spec/graph graph)]
    :post [(s/valid? ::solver-spec/priority-map %)]}
   (merge
     (priority-map-keyfn #(:distance %))
     (zipmap (keys graph) (repeat {:distance ##Inf :via nil})))))

(defn solve-dijkstra
  "Solves a maze using Dijkstra's algorithm"
  [{{start :start goal :goal} :maze-config passages :passages :as maze}]
  {:pre  [(s/valid? ::maze-spec/maze maze)]
   :post [(s/valid? ::solver-spec/priority-map %)]}
  ;; make a graph out of the given passages for shortest route computation
  (let [graph (passages->graph passages)]
    ;; solve usuing recursion, using two priority queues
    (loop [queue (assoc-in (gen-priority-map graph) [start :distance] 0)
           doneq (gen-priority-map)]
      ;; adjust course if the priority queue is empty
      (if (or  (empty? queue))
        ;; return the completion priority queue once all nodes have been visited
        doneq
        ;; compute the next pass if not
        (let
          ;; coord is the current visited item being looked at within the queue
          ;; neighbours is the unvisited sibling items of coord within the graph
          [[coord {dist :distance via :via}] (first queue)
           neighbours (filter
                        (fn [neighbour]
                          (not (contains? (into (hash-set) (keys doneq)) neighbour)))
                        (get graph coord))]
          ;; adjust course
          (cond
            ;; return the final iteration of the done queue
            (= coord goal) (conj doneq (first queue))
            ;; recur without computing new distances
            (empty? neighbours) (recur (pop queue) (conj doneq (first queue)))
            ;; recur and calculate new distances of unvisited neighbours
            :else (let [queue-items (into {} (map
                                               (fn [neighbour]
                                                 (let [{this-distance :distance via-coord :via :as queue-item} (get queue neighbour)]
                                                   ;; each neighbour has an implicit weight of 1 within the graph
                                                   (if (< (inc dist) this-distance)
                                                     {neighbour {:distance (inc dist) :via coord}}
                                                     queue-item)))
                                               neighbours))
                        queue' (merge queue queue-items)]
                    (recur (pop queue') (conj doneq (first queue'))))))))))

(defn priority-map->path
  "Returns an ordered collection of coordinates that represents a path through a maze"
  [priority-map goal]
  {:pre  [(s/valid? ::solver-spec/priority-map priority-map)
          (s/valid? ::maze-spec/coordinate goal)]
   :post [(s/valid? ::solver-spec/coord-seq %)]}
  (loop [map priority-map
         path [goal]
         current (get map (last path))]
    (if (empty? map)
      (reverse path)
      (let [coord (last path)
            {via :via} current]
        (if (= nil via)
          (reverse path)
          (recur (dissoc map coord) (conj path via) (get map via)))))))

(defn solve-maze
  "Solves the given maze using the given algorithm"
  [maze algorithm]
  {:pre  [(s/valid? ::maze-spec/maze maze)
          (s/valid? ::solver-spec/algorithm algorithm)]
   :post [(s/valid? ::solver-spec/coord-seq %)]}
  (->
    (cond
      (= algorithm "dijkstra") (solve-dijkstra maze))
    (priority-map->path (get-in maze [:maze-config :goal]))))