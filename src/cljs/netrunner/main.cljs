(ns netrunner.main
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [goog.events :as events])
  (:import goog.history.Html5History
           goog.history.EventType))

(def app-state
  (atom {:active-page "/"
         :user (js->clj js/user :keywordize-keys true)
         :cards [] :sets []
         :decks [] :decks-loaded false
         :games [] :gameid nil :messages []}))

(def tokens #js ["/" "/cards" "/deckbuilder" "/play" "/help" "/about" "/admin"])

(def history (Html5History.))

(defn navigate [token]
  (let [page-number (.indexOf tokens token)]
    (.carousel (js/$ ".carousel") page-number))
  (try (js/ga "send" "pageview" token) (catch js/Error e))
  (.setToken history token)
  (swap! app-state assoc :active-page [token]))

(events/listen history EventType/NAVIGATE #(navigate (.-token %)))
(.setUseFragment history false)
(.setPathPrefix history "")
(.setEnabled history true)

(defn navbar [cursor owner]
  (om/component
   (sab/html
    [:ul.carousel-indicator {}
     (for [page [["Weyland" "/" 0]
                 ["Cards" "/cards" 1]
                 ["Deck Builder" "/deckbuilder" 2]
                 ["Play" "/play" 3]
                 ["Help" "/help" 4]
                 ["About" "/about" 5]
                 ["Admin" "/admin" 6]]]
       (let [route (second page)]
         [:li {:class (if (= (first (:active-page cursor)) route) "active" "")
               :on-click #(.setToken history route)
               :data-target "#main" :data-slide-to (last page)}
          [:a {:href route} (first page)]]))])))

(defn status [cursor owner]
  (om/component
   (sab/html
    [:div
     [:div.float-right
      (let [c (count (:games cursor))]
        (str c " Game" (when (not= c 1) "s")))]
     (when-let [game (some #(when (= (:gameid cursor) (:gameid %)) %) (:games cursor))]
       (when (:started game)
         [:div.float-right
          (when (not= (:side @app-state) :spectator)
            [:a.concede-button {:on-click #(netrunner.gameboard/send-command "concede" {:user (:user @app-state)})} "Concede"])
          [:a {:on-click #(netrunner.gamelobby/leave-game)} "Leave game"]]))
     (when-let [game (some #(when (= (:gameid cursor) (:gameid %)) %) (:games cursor))]
       (when (:started game)
         (let [c (count (:spectators game))]
            (when (pos? c)
              [:div.spectators-count.float-right (str c " Spectator" (when (> c 1) "s"))
               [:div.blue-shade.spectators (om/build-all netrunner.gamelobby/player-view (:spectators game))]]))))])))

(defn- bioroid-string [id]
  (let [letters "ABCDEFGHIJKLMNPQRSTUVWXYZ"
        nums "0123456789"
        both "ABCDEFGHIJKLMNPQRSTUVWXYZ0123456789"]
    (str (.charAt nums (mod id (.-length nums)))
         (.charAt letters (mod id (.-length letters)))
         (clojure.string/join "" (map #(.charAt both (mod (* id %) (.-length both))) [2 3 4 5])))))

(defn- runner-type-string [id]
  (let [types ["Natural" "G-mod" "Cyborg" "Digital" "Bioroid" "Clone"]]
    (nth types (mod id (count types)))))

(defn- leet-string [name]
  (let [m {"i" "1" "a" "4" "e" "3" "t" "7" "o" "0" }]
    (clojure.string/join
                   "" (map #(if-let [rep (m %)] rep %) name))))

(defn get-username [user]
  (if-let [name (:username user)]
    (let [id-hex (.substring (:emailhash user) 0 6)
          id (int (str "0x" id-hex))]
      (case (mod id 6)
        0 (str "Chairman " (clojure.string/capitalize name))
        1 (str (clojure.string/capitalize name) " " (bioroid-string id))
        2 (str name " " (inc (mod id 3)) ".0")
        3 (str name ", " (runner-type-string id))
        4 (leet-string name)
        (str name ", NRDB Deck of the Week winner")))
    ""))

(om/root navbar app-state {:target (. js/document (getElementById "left-menu"))})
(om/root status app-state {:target (. js/document (getElementById "status"))})

