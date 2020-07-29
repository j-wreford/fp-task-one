(ns maze.core
  (:require [clojure.spec.alpha :as s]
            [maze.spec :as maze-spec]))

(defn coord-valid?
  "Returns true if the given coordinate is within the boundary dictated by size"
  [size coord]
  {:pre  [(s/valid? ::maze-spec/size size)
          (s/valid? ::maze-spec/coordinate coord)]
   :post [(boolean? %)]}
  (let [[row col] coord]
    (and
      (<= 0 row (dec (:rows size)))
      (<= 0 col (dec (:cols size))))))

(defn neighbour-coord
  "Returns a neighbouring coordinate at the given cardinal direction"
  [coord direction]
  {:pre  [(s/valid? ::maze-spec/coordinate coord)
          (s/valid? ::maze-spec/cardinal-direction direction)]
   :post [(s/valid? ::maze-spec/coordinate %)]}
  (cond
    (= direction :north) (assoc coord 0 (dec (first coord)))
    (= direction :south) (assoc coord 0 (inc (first coord)))
    (= direction :east)  (assoc coord 1 (inc (last coord)))
    (= direction :west)  (assoc coord 1 (dec (last coord)))))

(defn neighbour-coords
  "Returns a map of neighbouring coordinates keyed by their cardinal direction"
  [coord]
  {:pre  [(s/valid? ::maze-spec/coordinate coord)]
   :post [(s/valid? ::maze-spec/neighbour-coordinates %)]}
  (->>
    (map #(neighbour-coord coord %1) [:north :south :east :west])
    (interleave [:north :south :east :west])
    (apply hash-map)))

(defn valid-neighbour-coord
  "Returns a valid neighbour coordinate, or nil"
  [size coord direction]
  {:pre  [(s/valid? ::maze-spec/size size)
          (s/valid? ::maze-spec/coordinate coord)
          (s/valid? ::maze-spec/cardinal-direction direction)]
   :post [(s/valid? ::maze-spec/nilable-coordinate %)]}
  (let [neighbour (neighbour-coord coord direction)]
    (if (coord-valid? size neighbour)
      neighbour)))

(defn valid-neighbour-coords
  "Returns a map of valid or nil neighbouring coordinates keyed by their cardinal direction"
  [size coord]
  {:pre  [(s/valid? ::maze-spec/size size)
          (s/valid? ::maze-spec/coordinate coord)]
   :post [(s/valid? ::maze-spec/neighbour-coordinates %)]}
  (reduce-kv
    (fn [kvs k _]
      (assoc kvs k (valid-neighbour-coord size coord k)))
    {} (neighbour-coords coord)))

(defn safe-neighbour-coords
  "Calls valid-neighbour-coords with nil values removed"
  [size coord]
  {:pre  [(s/valid? ::maze-spec/size size)
          (s/valid? ::maze-spec/coordinate coord)]
   :post [(s/or ::maze-spec/cardinal-direction ::maze-spec/coordinate)]}
  (apply merge
         (for [[k v] (valid-neighbour-coords size coord)
               :when (not (nil? v))] {k v})))

(defn passage-exists?
  "Returns true when passages contains coord-pair or coord-pair reversed"
  [passages coord-pair]
  {:pre  [(s/valid? ::maze-spec/passages passages)
          (s/valid? ::maze-spec/coordinate-pair coord-pair)]
   :post [(or (boolean? %) (nil? %))]}
  (some
    #(or (= %1 coord-pair) (= (reverse %1) coord-pair))
    passages))

(defn passage-exists-at?
  "Returns true when passages contains a pairing of the given coord and a neighbour of it"
  [passages coord direction]
  {:pre  [(s/valid? ::maze-spec/passages passages)
          (s/valid? ::maze-spec/coordinate coord)
          (s/valid? ::maze-spec/cardinal-direction direction)]
   :post [(or (boolean? %) (nil? %))]}
  (passage-exists? passages (vector coord (neighbour-coord coord direction))))

(defn add-passage
  "Adds a passage between coord-pair to the given passages vector"
  [passages coord-pair]
  {:pre  [(s/valid? ::maze-spec/passages passages)
          (s/valid? ::maze-spec/coordinate-pair coord-pair)]
   :post [(s/valid? ::maze-spec/passages %)]}
  (if (not (passage-exists? passages coord-pair))
    (conj passages coord-pair)
    passages))

(defn gen-coords
  "Generates a 2d vector of coordinates that covers all points of a grid with the given size"
  [size]
  {:pre  [(s/valid? ::maze-spec/size size)]
   :post [(s/valid? ::maze-spec/coordinate-vector-2d %)]}
  (into [] (for [row (range (:rows size))]
    (into [] (for [col (range (:cols size))]
      [row col])))))

(defn gen-coords-seq
  "Returns gen-cords as a sequence of coordinates"
  [size]
  {:pre  [(s/valid? ::maze-spec/size size)]
   :post [(s/valid? ::maze-spec/coordinate-seq %)]}
  (map vec (partition 2 (flatten (gen-coords size)))))

(defn gen-passages-binary-tree-recur
  "Generates passages based on coin flip logic using recursion"
  [size]
  {:pre  [(s/valid? ::maze-spec/size size)]
   :post [(s/valid? ::maze-spec/passages %)]}
  (loop [passages []
         unprocessed-coords (gen-coords-seq size)]
    (if (empty? unprocessed-coords)
      passages
      (let [coord (first unprocessed-coords)
            neighbours (->
                         (safe-neighbour-coords size coord)
                         (select-keys [:north :east]))]
        (if (empty? neighbours)
          (recur passages (rest unprocessed-coords))
          (recur (add-passage passages [coord (rand-nth (vals neighbours))]) (rest unprocessed-coords)))))))

(defn gen-passages-binary-tree
  "Generates passages based on coin flip logic"
  [size]
  {:pre  [(s/valid? ::maze-spec/size size)]
   :post [(s/valid? ::maze-spec/passages %)]}
  (reduce
    (fn [passages coord]
      (let [neighbours (->
                         (safe-neighbour-coords size coord)
                         (select-keys [:north :east]))]
        (if (empty? neighbours)
          passages
          (add-passage passages (vector coord (rand-nth (vals neighbours)))))))
    [] (gen-coords-seq size)))

(defn gen-passages
  "Generates passages using the given algorithm and size"
  [algorithm-str size]
  {:pre  [(s/valid? ::maze-spec/algorithm algorithm-str)
          (s/valid? ::maze-spec/size size)]
   :post [(s/valid? ::maze-spec/passages %)]}
  (cond
    (= "binary-tree-recur" algorithm-str) (gen-passages-binary-tree-recur size)
    (= "binary-tree"       algorithm-str) (gen-passages-binary-tree size)))

(defn gen-maze
  "Generates a maze using the given maze configuration map"
  [config]
  {:pre  [(s/valid? ::maze-spec/maze-config config)]
   :post [(s/valid? ::maze-spec/maze %)]}
  {:maze-config config
   :passages (gen-passages (:algorithm config) (:size config))
   :generated-at (new java.util.Date)})

(gen-maze {:size {:rows 4 :cols 4} :start [0 0] :goal [3 3] :algorithm "binary-tree"})

(defn maze->string
  "Returns a string representation of a maze with the given size and passages"
  [maze]
  {:pre  [(s/valid? ::maze-spec/maze maze)]
   :post [(string? %)]}
  (let [size (get-in maze [:maze-config :size])
        passages (:passages maze)]
    (str
      "\n+" (apply str (repeat (:cols size) "---+"))
      (reduce
        (fn [a row]
          (->>
            (reduce
              (fn [b coord]
                ; build upon the top string
                [(str (first b) "   " (if (passage-exists-at? passages coord :east) " " "|"))
                 ; build upon the bottom string
                 (str (last b) (if (passage-exists-at? passages coord :south) "   " "---") "*")])
              ["\n|" "\n*"]
              row)
            ; join the top and bottom strings
            (apply str)
            ; join this row string with the rest of the row strings
            (str a)))
        ""
        (gen-coords size)))))