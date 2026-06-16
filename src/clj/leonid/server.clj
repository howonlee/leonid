(ns leonid.server
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp])
  (:gen-class))

;; --- Handlers -------------------------------------------------------------

(defn index-handler
  "Serve the SPA entry point."
  [_request]
  (-> (resp/resource-response "public/index.html")
      (resp/content-type "text/html")))

(defn ping-handler
  "A tiny JSON API the frontend calls to prove the round-trip works."
  [request]
  (let [n (get-in request [:body-params :n] 0)]
    {:status 200
     :body   {:message "pong from clojure backend"
              :n       (inc n)}}))

;; --- Routes & app ---------------------------------------------------------

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get index-handler}]
     ["/api/ping" {:post {:handler ping-handler}}]]
    {:data {:muuntaja   m/instance
            :middleware [muuntaja/format-middleware]}})
   ;; Fall back to static resources (the shadow-cljs JS output, etc.).
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))

(defonce server (atom nil))

(defn start!
  ([] (start! 3000))
  ([port]
   (reset! server
           (jetty/run-jetty #'app {:port port :join? false}))
   (println (str "Server running on http://localhost:" port))))

(defn stop! []
  (when-let [s @server]
    (.stop s)
    (reset! server nil)))

(defn -main [& _args]
  (start! 3000))
