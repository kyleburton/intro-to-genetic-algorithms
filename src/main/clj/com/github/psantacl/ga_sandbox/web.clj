(ns com.github.psantacl.ga-sandbox.web
  (:require
   [com.github.psantacl.ga-sandbox.log      :as log]
   [clojure.contrib.str-utils               :as str-utils]
   [compojure :as compojure])
  (:gen-class))

(defonce *log* (log/get-logger *ns*))
(defonce *server* (atom nil))

(defonce *log4j-config* (atom nil))

(defonce *compojure-config*
  (atom {"com.algorithmics.v4-interface.web.port" 8001}))

(defn reload-log4j-configuration []
  ;; TODO: look into using a property or xml configurator as well
  (if (not (nil? @*log4j-config*))
    (org.apache.log4j.PropertyConfigurator/configure @*log4j-config*)))

(defn page-footer []
  [:div
   [:h1 ""]
   [:ul
    [:li [:a {:href "/"} "Home"]]]])

(defn index-page [request params]
  (compojure/html
   [:h1 "Einstein Solver"]
   [:div
    [:p "This UI sucks, make it better, do some css and some jQuery please..."]]
   (page-footer)))

(compojure/defroutes my-app
  (compojure/GET "/"                                 (index-page request params)))

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




