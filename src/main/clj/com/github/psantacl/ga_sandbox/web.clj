(ns com.github.psantacl.ga-sandbox.web
  (:require
   [com.github.psantacl.ga-sandbox.log           :as log]
   [clojure.contrib.str-utils                    :as str-utils]
   [compojure                                    :as compojure]
   [clojure.contrib.json.write                   :as wjs]
   [com.github.psantacl.ga-sandbox.framework     :as ga]
   [com.github.psantacl.ga-sandbox.einstein-main :as main])
  (:gen-class))

(defonce *log* (log/get-logger *ns*))
(defonce *server* (atom nil))

(defonce *log4j-config* (atom nil))

(defonce *compojure-config*
  (atom {:port 8001}))

(defonce *curr-generation*
  (atom {:generation-number 0
         :best-genome       [0.00 (main/einstein-random-genome)]
         :best-fitness      nil
         :rest-of-genome    nil
         :best-scores       [[0] 0.00]
         :avg-scores        [0.00]
         :avg-score         0.00
         :params            nil}))

(defn fitness-report-by-predicate [genome]
  (map (fn [spec]
         {:passed (if ((:pred spec) (main/get-houses genome))
                    true false)
          :name (:name spec)})
       main/*all-fitness-predicates*))

;;   (doseq [spec main/*all-fitness-predicates*]
;;     (if (not ((:pred spec) (main/get-houses (second best))))
;;       (println (format "  failed: %s" (:name spec)))))

(defn reset!-curr-generation []
  (let [g (main/einstein-random-genome)]
   (reset! *curr-generation*
           {:generation-number 0
            :best-genome       [(main/einstein-fitness-score g) g]
            :best-fitness      (fitness-report-by-predicate g)
            :rest-of-genome    nil
            :best-scores       [[0] 0.00]
            :avg-scores        [0.00]
            :avg-score         0.00
            :params            nil})))

;; (let [g (main/einstein-random-genome)] (fitness-report-by-predicate g))
;; (let [g (main/einstein-random-genome)] g)
;; (let [g (main/einstein-random-genome)] (main/einstein-fitness-score g))
;; (let [g (main/einstein-random-genome)] ((:pred (first main/*all-fitness-predicates*))) (main/get-houses g))
;; (reset!-curr-generation)

(defn reset-curr-stats! []
  (reset! *curr-generation*
          {:generation-number 0
           :best-genome       [0.00 (main/einstein-random-genome)]
           :rest-of-genome    nil
           :best-scores       [[0] 0.00]
           :avg-scores        [0.00]
           :avg-score         0.00
           :params            nil}))

(defn reload-log4j-configuration []
  ;; TODO: look into using a property or xml configurator as well
  (if (not (nil? @*log4j-config*))
    (org.apache.log4j.PropertyConfigurator/configure @*log4j-config*)))

(defmacro defpage [name title & body]
  `(defn ~name [~'request ~'params]
     (compojure/html
      [:html
       [:head
        [:link { :href "/stylesheets/app.css" :media "screen" :rel "stylesheet" :type "text/css"}]
        [:title ~title]]
       [:body
        (compojure/html ~@body)
        [:script {:src "/javascript/jquery-1.4.2.min.js"}]
        [:script {:src "/javascript/Jaml-all.js"}]
        [:script {:src "/javascript/raphael-min.js"}]
        [:script {:src "/javascript/g.raphael-min.js"}]
        [:script {:src "/javascript/g.bar-min.js"}]
        [:script {:src "/javascript/g.dot-min.js"}]
        [:script {:src "/javascript/g.line-min.js"}]
        [:script {:src "/javascript/g.pie-min.js"}]
        [:script {:src "/javascript/json2.js"}]
        [:script {:src "/javascript/ga.js"}]]])))

(defpage index-page "GA Einstein's Puzzle"
  [:h1 "Einstein's Puzzle - Current Status"]
  [:div
  [:div
   [:table
    [:tr [:td "Generations"] [:td {:id "generation-number"} "&nbsp;"]]
    [:tr [:td "Best Score"]  [:td {:id "best-score"} "&nbsp;"]]
    [:tr [:td "Avg Score"]   [:td {:id "avg-score"} "&nbsp;"]]]]]
  [:div {:class "clear-both"}]
  [:div
   [:div {:id "best-genome"}]
   [:div {:id "raphael-graph"}]]
  [:div {:class "clear-both"}]
  [:div {:id "messages"}]
  [:div
   [:ul
    [:li [:a {:id "refresh-button"  :href "#"} "Refresh"]]
    [:li [:a {:href "/"} "Home"]]]]
[:div
[:pre
 [:a {:href "http://www.stanford.edu/~laurik/fsmbook/examples/Einstein%27sPuzzle.html"} "Einstein's Puzzle"]
 "
    1. The Englishman lives in the red house.
    2. The Swede keeps dogs.
    3. The Dane drinks tea.
    4. The green house is just to the left of the white one.
    5. The owner of the green house drinks coffee.
    6. The Pall Mall smoker keeps birds.
    7. The owner of the yellow house smokes Dunhills.
    8. The man in the center house drinks milk.
    9. The Norwegian lives in the first house.
   10. The Blend smoker has a neighbor who keeps cats.
   11. The man who smokes Blue Masters drinks bier.
   12. The man who keeps horses lives next to the Dunhill smoker.
   13. The German smokes Prince.
   14. The Norwegian lives next to the blue house.
   15. The Blend smoker has a neighbor who drinks water.

 5 houses
  - color
  - nationality
  - drink
  - tobacco
  - pet
"]]
  )


(defn web-root []
  (format "%s/src/main/resources" (System/getProperty "com.github.kyleburton.project-dir")))

(defn serve-file [path]
  (compojure/serve-file (web-root) path))

(compojure/defroutes my-app
  (compojure/GET "/"                            (index-page request params))
  (compojure/GET "/ga/current-generation.json"  (wjs/json-str @*curr-generation*))
  (compojure/GET "/*"                           (or (serve-file (params :*))
                                                    :next))
  (compojure/ANY "*"                            (compojure/page-not-found)))

(defn start-server []
  (log/infof "Starting compojure server: %s" @*compojure-config*)
  (reset! *server*
          (compojure/run-server
           @*compojure-config*
           "/*"
           (compojure/servlet my-app))))


(defn stop-server []
  (compojure/stop @*server*)
  (reset! *server* nil))

;; need a background thread for running the simluation
;; and a set of functions for interacting with it
;;   the report fn can just update the current-state atom
(defn ga-report-fn [generation-number [best & not-best] params]
  (printf (format "best=%s" best))
  (let [avg-score (/ (apply + (first best) (map first not-best))
                     (inc (count not-best)))]
    (reset!
     *curr-generation*
     (merge
      @*curr-generation*
      {:generation-number generation-number
       :best-genome       best
       :best-scores       (conj (:best-scores @*curr-generation*) [generation-number (first best)])
       :best-fitness      (fitness-report-by-predicate (second best))
       ;; (map #(prn (:pred %)) main/*all-fitness-predicates*)
       :rest-of-genome    "too big, sorry"              ;; not-best
       :params            "can't make params into json" ;; params
       :avg-score         avg-score
       :avg-scores        (conj (:avg-scores @*curr-generation*) [generation-number avg-score])
       })))
  (println (format "best[%s] %s" generation-number best))
  (doseq [spec main/*all-fitness-predicates*]
    (if (not ((:pred spec) (main/get-houses (second best))))
      (println (format "  failed: %s" (:name spec))))))

;; TODO allow these properties to be tweaked in the UI
;; TODO notice when the simulation has stopped all by itself...
(defn simulation []
  (ga/run-simulation
   (ga/gen-population 1000 main/einstein-random-genome)
   {:stop-score     1.0
    :max-iterations 2000                 ; 3000
    :survival-fn    (fn [ranked-population] (ga/random-weighted-survives ranked-population (* 0.80 (count ranked-population))))
    :mutator-fn     (fn [genome] (main/mutate-genome+chromosome-swap genome 0.40 0.30))
    :report-fn      ga-report-fn
    :fitness-fn     main/einstein-fitness-score}))

(defonce *simulation-thread* (atom nil))

(defn start-simulation []
  (if @*simulation-thread*
    {:result :error
     :message (format "Error: simulation is already running: %s" @*simulation-thread*)}
    (do
      (reset! *simulation-thread* (Thread. simulation))
      (.start @*simulation-thread*)
      {:result :success})))

(defn stop-simulation []
  (if (not @*simulation-thread*)
    {:result :error
     :message (format "Error: simulation is already running: %s" @*simulation-thread*)}
    (do
      (if (.isAlive @*simulation-thread*)
        (ga/stop-simulation))
      (log/infof "'joining' simulation thread, awaiting p to 5s")
      (.join @*simulation-thread* 5000)
      (if (.isAlive @*simulation-thread*)
        {:result :error
         :message (format "Error: " @*simulation-thread*)}
        (do
          (reset! *simulation-thread* nil)
          (reset-curr-stats!)
          {:result :success})))))


(comment


  (start-server)

  (stop-server)

  (wjs/json-str @*curr-generation*)

  (.isAlive @*simulation-thread*)

  (reset!-curr-generation)
  *curr-generation*
  @*curr-generation*

  (:best-fitness @*curr-generation*)

  (start-simulation)

  (stop-simulation)


)




