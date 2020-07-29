(ns solver.spec
  (:require [maze.spec :as maze-spec]
            [clojure.spec.alpha :as s]))

;; allowed algorithms for solving
(s/def ::algorithm #{"dijkstra"})

;; a graph generated from maze passages
(s/def ::graph (s/every-kv ::maze-spec/coordinate (s/coll-of ::maze-spec/coordinate)))

;; an item within a priority map
(s/def ::pos-int (s/int-in 0 Integer/MAX_VALUE))
(s/def ::inf #(= % ##Inf))
(s/def ::distance (s/or :pos-int ::pos-int :infinite? ::inf))

(s/def ::via ::maze-spec/nilable-coordinate)
(s/def ::priority-map-item (s/keys :req-un [::distance ::via]
                                   :opt-un []))

;; a priority map
(s/def ::priority-map (s/every-kv ::maze-spec/coordinate ::priority-map-item))

;; coordinate-seq
(s/def ::coord-seq (s/and seq? (s/coll-of ::maze-spec/coordinate)))