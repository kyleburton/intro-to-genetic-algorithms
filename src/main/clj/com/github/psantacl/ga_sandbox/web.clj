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
  (atom {:port 8001}))

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
        (compojure/html ~@body)]
       [:div
        [:ul
         [:li [:a {:href "/"} "Home"]]]]
       [:script {:src "/javascript/jquery-1.4.2.min.js"}]])))

(defpage index-page "Einstein Solver"
  [:h1 "Einstein Solver"]
  [:div
   [:p "This UI sucks, make it better, do some css and some jQuery please..."]])

(defn web-root []
  (format "%s/src/main/resources" (System/getProperty "com.github.kyleburton.project-dir")))

(defn serve-file [path]
  (compojure/serve-file (web-root) path))

(compojure/defroutes my-app
  (compojure/GET "/"                           (index-page request params))
  (compojure/GET "/*"                          (or (serve-file (params :*))
                                                   :next))
  (compojure/ANY "*"                           (compojure/page-not-found)))

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

(comment


  (start-server)

  (stop-server)


)




