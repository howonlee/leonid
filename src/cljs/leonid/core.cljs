(ns leonid.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdc]))

(defonce state (r/atom {:n 0 :message "click the button"}))

(defn ping!
  "POST the current counter to the backend and store the response."
  []
  (-> (js/fetch "/api/ping"
                #js {:method  "POST"
                     :headers #js {"Content-Type" "application/json"}
                     :body    (js/JSON.stringify #js {:n (:n @state)})})
      (.then #(.json %))
      (.then (fn [data]
               (let [d (js->clj data :keywordize-keys true)]
                 (swap! state assoc :n (:n d) :message (:message d)))))))

(defn app []
  [:div {:style {:font-family "sans-serif" :padding "2rem"}}
   [:h1 "leonid"]
   [:p "Clojure backend + ClojureScript frontend."]
   [:p "Count: " [:strong (:n @state)]]
   [:p "Backend says: " [:em (:message @state)]]
   [:button {:on-click ping!} "Ping backend"]])

(defonce root (rdc/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load render []
  (rdc/render root [app]))

(defn init []
  (render))
