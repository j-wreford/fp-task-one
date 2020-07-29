(ns maze.spec
  (:require [clojure.spec.alpha :as s]))

;; cardinal direction keys
(s/def ::cardinal-direction #{:north :south :east :west})

;; size map with rows and cols
(s/def ::size (s/map-of #{:rows :cols} pos-int?))

;; coordinate vector
(s/def ::coordinate (s/and vector?
                           (s/coll-of int?)
                           #(= 2 (count %))))

;; nilable coordinate vector
(s/def ::nilable-coordinate (s/nilable ::coordinate))

;; a pair of coordinates
(s/def ::coordinate-pair (s/and vector?
                                (s/coll-of ::coordinate)
                                #(= 2 (count %))))

;; a sequence of coordinates
(s/def ::coordinate-seq (s/and seq?
                               (s/coll-of ::coordinate)))

;; a vector of coordinates
(s/def ::coordinate-vector (s/and vector?
                                  (s/coll-of ::coordinate)))

;; a 2d vector of coordinates
(s/def ::coordinate-vector-2d (s/and vector?
                                     (s/coll-of ::coordinate-vector)))

;; neighbour coordinate map
(s/def ::neighbour-coordinates (s/map-of #{:north :south :east :west} ::nilable-coordinate))

;; maze configuration map
(s/def ::start ::coordinate)
(s/def ::goal ::coordinate)
(s/def ::algorithm #{"binary-tree-recur" "binary-tree"})
(s/def ::maze-config
  (s/keys :req-un [::size ::start
                   ::goal ::algorithm]
          :opt-un []))

;; maze passages
(s/def ::passages
  (s/and vector?
         (s/coll-of ::coordinate-pair)))

;; maze map
(s/def ::_id string?)
(s/def ::generated-at (or string? #(instance? java.util.Date %)))
(s/def ::maze (s/keys :req-un [::maze-config ::passages]
                      :opt-un [::_id ::generated-at]))