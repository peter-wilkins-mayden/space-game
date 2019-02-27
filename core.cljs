(ns my-game.core
  (:require [play-cljs.core :as p]
            [goog.events :as events]))

(defonce game (p/create-game (.-innerWidth js/window) (.-innerHeight js/window)))
(defonce state (atom {}))

(defn correct-left [a]
  (mod (- 10 a) 360))

(defn correct-right [a]
  (mod (+ 10 a) 360))

(events/listen js/window "keydown"
               (fn [key-event]
                 (let [key-pressed (.-key (.-event_ key-event))]
                   (case key-pressed
                     "," (do
                           (js/console.log (-> @state :ship :direction))
                           (swap! state update-in [:ship :direction] correct-left))
                     "." (do (js/console.log (-> @state :ship :direction))
                             (swap! state update-in [:ship :direction] correct-right))
                     nil))))

(def main-screen
  (reify p/Screen
    (on-show [this]
      (reset! state {:planets [{:x 100 :y 100 :r 30} {:x 300 :y 400 :r 60} {:x 800 :y 240 :r 100}]
                     :ship {:x 10 :y 10 :speed {:x 1 :y -1} :direction 130}}))
    (on-hide [this])
    (on-render [this]
      (swap! state update-in [:ship :speed :x] #(if (> (-> @state :ship :direction) 180) (- % 1) (+ % 1)))
      (swap! state update-in [:ship :speed :y] #(let [d (-> @state :ship :direction)]
                                                  (if (or (> d 90) (< 270)) (- % 1) (+ % 1))))

      (swap! state update-in [:ship :x] #(+ % (-> @state :ship :speed :x)))
      (swap! state update-in [:ship :y] #(+ % (-> @state :ship :speed :y)))
      (p/render game
                [[:fill {:color "darkblue"}
                  [:rect {:x 0 :y 0 :width (.-innerWidth js/window) :height (.-innerHeight js/window)}]]
                 (for [p (:planets @state)] [:planet p])
                 [:ship (:ship @state)]
                 ]))))

(events/listen js/window "mousemove"
               (fn [event]
                 (swap! state assoc :text-x (.-clientX event) :text-y (.-clientY event))))

(events/listen js/window "resize"
               (fn [event]
                 (p/set-size game js/window.innerWidth js/window.innerHeight)))

;; start the game

(doto game
  (p/start)
  (p/set-screen main-screen))

(defmethod
  p/draw-sketch!
  :planet
  [game renderer content parent-opts]
  (let
    [[_ opts & children]
     content
     opts
     (play-cljs.options/update-opts
       opts
       parent-opts
       play-cljs.options/basic-defaults)]
    (p/draw-sketch!
      game
      renderer
      [:div
       {:x 100, :y 100}
       [:fill
        {:color "green"}
        [:ellipse
         {:width (* 1.8 (:r opts)), :height (* 1.8 (:r opts))}]]
       [:ellipse
        {:width (* 1.4 (:r opts)), :height (* 1.4 (:r opts))}]
       [:fill
        {:color "yellow"}
        [:ellipse
         {:width (:r opts), :height (:r opts)}]]]
      opts)
    (p/draw-sketch! game renderer children opts)))

(defmethod
  p/draw-sketch!
  :ship
  [game renderer content parent-opts]
  (let
    [[_ {{x :x y :y speed :speed [x y] :direction} :ship :as opts} & children]
     content
     opts
     (play-cljs.options/update-opts
       opts
       parent-opts
       play-cljs.options/basic-defaults)]
    (p/draw-sketch!
      game
      renderer
      [:div
       {:x x, :y y}
       [:fill
        {:color "red"}
        [:triangle {:x1 10, :y1 10, :x2 50, :y2 25, :x3 10, :y3 35}]]]
      opts)
    (p/draw-sketch! game renderer children opts)))
