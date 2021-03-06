(ns com.github.psantacl.ga-sandbox.einstein-main
  (:require
   [com.github.psantacl.ga-sandbox.framework :as ga])
  (:gen-class)
  (:use
     clojure.contrib.duck-streams
     clojure.contrib.str-utils))

;; http://www.stanford.edu/~laurik/fsmbook/examples/Einstein%27sPuzzle.html

;;    1. The Englishman lives in the red house.
;;    2. The Swede keeps dogs.
;;    3. The Dane drinks tea.
;;    4. The green house is just to the left of the white one.
;;    5. The owner of the green house drinks coffee.
;;    6. The Pall Mall smoker keeps birds.
;;    7. The owner of the yellow house smokes Dunhills.
;;    8. The man in the center house drinks milk.
;;    9. The Norwegian lives in the first house.
;;   10. The Blend smoker has a neighbor who keeps cats.
;;   11. The man who smokes Blue Masters drinks bier.
;;   12. The man who keeps horses lives next to the Dunhill smoker.
;;   13. The German smokes Prince.
;;   14. The Norwegian lives next to the blue house.
;;   15. The Blend smoker has a neighbor who drinks water.

;; 5 houses
;;  - color
;;  - nationality
;;  - drink
;;  - tobacco
;;  - pet


(def *colors*            (ga/split-pipe-str "blue   | green      | red     | white    | yellow"))
(def *colors-set*        (ga/list->set *colors*))
(def *nationalities*     (ga/split-pipe-str "Dane   | Englishman | German  | Swede    | Norwegian"))
(def *nationalities-set* (ga/list->set *nationalities*))
(def *drinks*            (ga/split-pipe-str "bier   | coffee     | milk    | tea      | water"))
(def *drinks-set*        (ga/list->set *drinks*))
(def *tobaccos*          (ga/split-pipe-str "Blend  | BlueMaster | Dunhill | PallMall | Prince"))
(def *tobaccos-set*      (ga/list->set *tobaccos*))
(def *pets*              (ga/split-pipe-str "birds  | cats       | dogs    | fish     | horses"))
(def *pets-set*          (ga/list->set *pets*))

(def *attributes* {:color       *colors*
                   :nationality *nationalities*
                   :drink       *drinks*
                   :tobacco     *tobaccos*
                   :pet         *pets*})

(def *all-attribute-values*
     (apply concat (vals *attributes*)))

(defn random-house []
  [(ga/rand-elt (:color       *attributes*))
   (ga/rand-elt (:nationality *attributes*))
   (ga/rand-elt (:drink       *attributes*))
   (ga/rand-elt (:tobacco     *attributes*))
   (ga/rand-elt (:pet         *attributes*))])

(def *genome-template*
     (vec (apply concat (for [x (range 5)]
                          [(:color       *attributes*)
                           (:nationality *attributes*)
                           (:drink       *attributes*)
                           (:tobacco     *attributes*)
                           (:pet         *attributes*)]))))

;; (random-house)

(defn einstein-random-genome []
  (vec (apply concat (for [x (range 5)]
                       (random-house)))))


(defn get-house [genome house-number]
  (let [offset (* house-number 5)]
    (vec (for [idx (range offset (+ offset 5))]
           (nth genome idx)))))

(defn get-houses [genome]
  (vec (map #(get-house genome %) (range 0 5))))


(defn house-color       [house] (nth house 0))
(defn house-nationality [house] (nth house 1))
(defn house-drink       [house] (nth house 2))
(defn house-tobacco     [house] (nth house 3))
(defn house-pet         [house] (nth house 4))

(defn house-position [houses pred]
  (loop [pos 0
         [house & houses] houses]
    (cond (pred house)   pos
          (not house)    nil
          true           (recur (inc pos) houses))))

(defn has-neighbor [houses house-pos pred]
  (cond (not house-pos)                         false
        (= 0 house-pos)                         (pred (nth houses 1))
        (= 4 house-pos)                         (pred (nth houses 3))
        (pred (nth houses (dec house-pos)))     true
        (pred (nth houses (inc house-pos)))     true
        :else                                   false))


(def *einstein-score-fns*
     [{:name "1. The Englishman lives in the red house."
       :pred (fn [houses]
               (some #(and (= :Englishman (house-nationality %))
                           (= :red        (house-color %)))
                     houses))}
      {:name "2. The Swede keeps dogs."
       :pred
       (fn [houses]
         (some #(and (= :Swede (house-nationality %))
                     (= :dogs  (house-pet %)))
               houses))}
      {:name "3. The Dane drinks tea."
       :pred
       (fn [houses]
         (some #(and (= :Dane (house-nationality %))
                     (= :tea  (house-drink %)))
               houses))}
      {:name "4. The green house is just to the left of the white one."
       :pred
       (fn [houses]
         (let [green-pos (house-position houses #(= :green (house-color %)))
               white-pos (house-position houses #(= :white (house-color %)))]
           (and green-pos
                white-pos
                (= 1 (- white-pos green-pos)))))}
      {:name "5. The owner of the green house drinks coffee."
       :pred
       (fn [houses]
         (some #(and (= :green   (house-color %))
                     (= :coffee  (house-drink %)))
               houses))}
      {:name "6. The Pall Mall smoker keeps birds."
       :pred
       (fn [houses]
         (some #(and (= :PallMall   (house-tobacco %))
                     (= :birds      (house-pet %)))
               houses))}
      {:name "7. The owner of the yellow house smokes Dunhill."
       :pred
       (fn [houses]
         (some #(and (= :yellow    (house-color %))
                     (= :Dunhill  (house-tobacco %)))
               houses))}
      {:name "8. The man in the center house drinks milk."
       :pred
       (fn [houses]
         (= :milk (house-drink (nth houses 2))))}
      {:name "9. The Norwegian lives in the first house."
       :pred
       (fn [houses]
         (= :Norwegian (house-nationality (nth houses 0))))}
      {:name "10. The Blend smoker has a neighbor who keeps cats."
       :pred
       (fn [houses]
         (has-neighbor houses
                       (house-position houses #(= :Blend (house-tobacco %)))
                       #(= :cats (house-pet %))))}
      {:name "11. The man who smokes Blue Masters drinks bier."
       :pred
       (fn [houses]
         (some #(and (= :BlueMaster    (house-tobacco %))
                     (= :bier          (house-drink %)))
               houses))}
      {:name "12. The man who keeps horses lives next to the Dunhill smoker."
       :pred
       (fn [houses]
         (has-neighbor houses
                       (house-position houses #(= :horses (house-pet %)))
                       #(= :Dunhill (house-tobacco %))))}
      {:name "13. The German smokes Prince."
       :pred
       (fn [houses]
         (some #(and (= :Prince    (house-tobacco %))
                     (= :German    (house-nationality %)))
               houses))}
      {:name "14. The Norwegian lives next to the blue house."
       :pred
       (fn [houses]
         (has-neighbor houses
                       (house-position houses #(= :blue (house-color %)))
                       #(= :Norwegian (house-nationality %))))}
      {:name "15. The Blend smoker has a neighbor who drinks water."
       :pred
       (fn [houses]
         (has-neighbor houses
                       (house-position houses #(= :Blend (house-tobacco %)))
                       #(= :water (house-drink %))))}])

(def *uniqueness-predicates*
  [
   {:name "unique.1 color"
    :pred (fn [houses] (= (count (ga/list->set (map house-color       houses))) (count houses)))}
   {:name "unique.2 nationality"
    :pred (fn [houses] (= (count (ga/list->set (map house-nationality houses))) (count houses)))}
   {:name "unique.3 drink"
    :pred (fn [houses] (= (count (ga/list->set (map house-drink       houses))) (count houses)))}
   {:name "unique.4 tobacco"
    :pred (fn [houses] (= (count (ga/list->set (map house-tobacco     houses))) (count houses)))}
   {:name "unique.5 pet"
    :pred (fn [houses] (= (count (ga/list->set (map house-pet         houses))) (count houses)))}
   ])

(def *all-fitness-predicates* (concat *einstein-score-fns* *uniqueness-predicates*))

;; NB: can also score on uniqueness of data values (eg: 1 red house, 1 with Englishman)
(defn einstein-fitness-score [genome]
  (ga/log "einstein-fitness-score: genome=%s" genome)
  (let [houses (get-houses genome)
        score (count (filter
                      (fn [test]
                        (let [{:keys [name pred]} test]
                          (if (pred houses)
                            (do
                              (ga/log "%s :hit!" name)
                              true)
                            (do
                              (ga/log "%s: miss" name)
                              false))))
                      *all-fitness-predicates*))]
    (/ (* 1.0 score) (count *all-fitness-predicates*))))

(defn mutate-genome-1 [genome mutation-rate chromosome-mutation-rate]
  (if (<= (ga/rand-float) mutation-rate)
    (vec (map (fn [chromosome]
                (if (<= (ga/rand-float) chromosome-mutation-rate)
                  (ga/rand-elt *all-attribute-values*)
                  chromosome))
              genome))
    genome))

(defn mutate-genome [genome mutation-rate chromosome-mutation-rate]
  (if (<= (ga/rand-float) mutation-rate)
    (vec (for [idx (range (count genome))]
           (if (<= (ga/rand-float) chromosome-mutation-rate)
                   (ga/rand-elt (nth *genome-template* idx))
                   (nth genome idx))))
    genome))

;; swap a whole chromosome (i.e. swap the location of 2 houses)
;; TODO: also (sometimes) swap a single gene (correct termonology?) instead of a complete chromosome
(defn random-chromosome-swap [genome]
  (let [ch1 (* 5 (rand-int 5))
        ch2 (* 5 (rand-int 5))]
      (assoc genome
        (+ ch1 0) (nth genome (+ ch2 0))    (+ ch2 0) (nth genome (+ ch1 0))
        (+ ch1 1) (nth genome (+ ch2 1))    (+ ch2 1) (nth genome (+ ch1 1))
        (+ ch1 2) (nth genome (+ ch2 2))    (+ ch2 2) (nth genome (+ ch1 2))
        (+ ch1 3) (nth genome (+ ch2 3))    (+ ch2 3) (nth genome (+ ch1 3))
        (+ ch1 4) (nth genome (+ ch2 4))    (+ ch2 4) (nth genome (+ ch1 4)))))

(defn mutate-genome+chromosome-swap [genome mutation-rate chromosome-mutation-rate]
  (if (<= (ga/rand-float) mutation-rate)
    (if (ga/flip-coin) ;; make this another parameter? -- the swap rate?
      (random-chromosome-swap genome)
      (vec (for [idx (range (count genome))]
             (if (<= (ga/rand-float) chromosome-mutation-rate)
               (ga/rand-elt (nth *genome-template* idx))
               (nth genome idx)))))
    genome))

(def *mutation-rate* (atom 0.5))
(def *chromosome-mutation-rate* (atom 0.25))

(def zeroed-distribution (sorted-map  0 0 0.05 0 0.1 0 0.15 0 0.2 0 0.25 0 0.3 0 0.35 0 0.4 0 0.45 0 0.5 0
                   0.55 0 0.6 0 0.65 0 0.7 0 0.75 0 0.8 0 0.85 0 0.9 0 0.95 0 1.0 0))


(defn pretty-print-map [da-map]
  (str-join "" (reverse (reduce (fn [l key] (cons (format "%-5s" (da-map key)) l))
                         '() (keys da-map)))))


(defn log-distributions [generation-number ranked-population]
  (append-spit "data.txt" (format "%-3s: %s\n"
                           generation-number
                           (pretty-print-map (reduce (fn [map key]
                                                       (assoc map key (inc (or (map key) 0))))
                                                     zeroed-distribution (map #(first %) ranked-population ))))))

(comment

  (do
    (prn "starting simulation")
    (time (ga/run-simulation (ga/gen-population 1000 einstein-random-genome)
                          {:stop-score     1.0
                           :max-iterations 3000 ; 3000
                           :survival-fn    (fn [ranked-population] (ga/random-weighted-survives ranked-population (* 0.80 (count ranked-population))))
                           :mutator-fn     (fn [genome] (mutate-genome+chromosome-swap genome 0.40 0.30))
;;                            :report-fn      (fn [generation-number [best & not-best] params]
;;                                              (println (format "best[%s]: %s" generation-number best))
;;                                              (doseq [spec *all-fitness-predicates*]
;;                                                (if (not ((:pred spec) (get-houses (second best))))
;;                                                  (println (format "  failed: %s" (:name spec))))))
;;                            :report-fn      (fn [generation-number [best & not-best] params]
;;                                              (println (format "best[%s]: %s" generation-number best))
;;                                              (println (format "chicken %s:" (reduce (fn [map key]
;;                                                                                        (assoc map key (inc (or (map key) 0))))
;;                                                                                      zeroed-distribution (map #(first %) (cons best not-best)) )
;;                                                               ))
                           :report-fn        (fn [generation-number [best & not-best] params]
                                               ;; (log-distributions generation-number (cons best not-best))
                                               (println (format "best[%s] %s" generation-number best))
                                               (doseq [spec *all-fitness-predicates*]
                                                 (if (not ((:pred spec) (get-houses (second best))))
                                                   (println (format "  failed: %s" (:name spec))))))
                           :fitness-fn     einstein-fitness-score})))

  (ga/stop-simulation)


  ;; max=1k
  ;;  run.1 solution in 122 generations
  ;;  run.2 no solution after 1k generations
  ;;  run.3 no solution after 1k generations
  ;;  run.4 no solution after 1k generations
  ;; max=2k
  ;;  run.1 solution in 118
  ;;  run.2 no solution after 2k
  ;;  run.2 solution in 206
  ;; max=3k
  ;;  run.1 solution in 154 generations
  ;;  run.2 no solution after 3k
  ;;  run.3 solution in 136 generations
  ;;  run.4 solution in 180 generations
  ;;  run.4 solution in 231 generations
  ;;  run.5 no solution after 3k
  ;;  run.6 solution in 232 generations
  ;;  run.7 solution in 114 generations
  ;;  run.7 solution in 2317 generations

  ;; Other ideas for testing / tweaking / reporting
  ;; do several runs, vary the core params (survival rate, mutation probability and rate, swap rate)
  ;;   track (and plot):
  ;;     rate of improvement
  ;;     diversity of population
  ;;     # of generations necessary to reach a solution
  ;; vary aspects like:
  ;;   enable / disable: valid chromosome swap (make it random)
  ;;   enable / disable: genome template (just use a random property)
  ;;
  ;; Allow an initial population to be supplied rather than be
  ;; generated randomly this would allow you to play with other
  ;; starting points (should reduce the variability between test runs
  ;; with other parametric variations).

  ;; TODO: the fitness functions are very slow, consider not
  ;; marshalling to/from a list of houses, just access the genome
  ;; vector directly from those functions, it should be significatly
  ;; faster.

  )


(def *positions* [ :color :nationality :drink :tobacco :pet ])




(defn -main [& args]
  (do
    (prn "starting simulation")
    (time
     (ga/run-simulation
      (ga/gen-population 1000 einstein-random-genome)
      {:stop-score     1.0
       :max-iterations 500 ; 3000
       :survival-fn    (fn [ranked-population] (ga/random-weighted-survives ranked-population (* 0.80 (count ranked-population))))
       :mutator-fn     (fn [genome] (mutate-genome+chromosome-swap genome 0.40 0.30))
       :report-fn        (fn [generation-number [best & not-best] params]
                           (println (format "best[%s] %s" generation-number best))
                           (doseq [spec *all-fitness-predicates*]
                             (if (not ((:pred spec) (get-houses (second best))))
                               (println (format "  failed: %s" (:name spec))))))
       :fitness-fn     einstein-fitness-score}))))




