(ns netrunner.admin
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [netrunner.ajax :refer [GET]]))
(def editors ["mtgred" "nealterrell" "Saintis" "zaroth" "JoelCFC25" "queueseven"
              "mediohxcore" "d1en's cat" "Apex"])

(defn- runner-winrates []
  [["Whizzard: Master Gamer" (+ 80 (rand 10))]
   ["MaxX: Maximum Punk Rock" (+ 75 (rand 20))]
   ["Valencia Estevez: The Angel of Cayambe" (+ 70 (rand 5))]
   ["Noise: Hacker Extraordinaire" (+ 68 (rand 23))]
   ["The Professor: Keeper of Knowledge" (+ 50 (rand 50))]])

(defn- corp-winrates []
  [["Haas-Bioroid: Engineering the Future" (+ 60 (rand 10))]
   ["Cerebral Imaging: Infinite Frontiers" 7]])

(defn- rng-settings []
  {:capap (rand-int 6)
   :snipe (zero? (rand-int 2))
   :siphon (zero? (rand-int 2))})

(defn- connect-settings []
  {:disconnect (zero? (rand-int 2))
   :blame (zero? (rand-int 2))})

(defn- new-state []
  {:runner-winrates (runner-winrates)
   :corp-winrates (corp-winrates)
   :rng (rng-settings)
   :connect (connect-settings)
   :editor (first (shuffle editors))})

(def app-state (atom (new-state)))

(defn admin [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (js/setInterval
        (fn [] (om/set-state! owner :editor (first (shuffle editors))))
        60000)
      (js/setInterval
        (fn []
          (om/set-state! owner :corp-winrates (corp-winrates))
          (om/set-state! owner :runner-winrates (runner-winrates))
          (om/set-state! owner :rng (rng-settings))
          (om/set-state! owner :connect (connect-settings)))
        10000))
    om/IInitState
    (init-state [this]
      (new-state))

    om/IRenderState
    (render-state [this state]
      (sab/html
      [:div.admin.panel.blue-shade
       [:h1 "Administration"]

       [:p "Currently being edited by " [:b {:style {:color "red"}} (om/get-state owner :editor)]]
       [:h2 "Winrate reports"]
       [:h3 "Runner"]
       [:ol
        (for [run (sort-by second > (om/get-state owner :runner-winrates))]
          [:li (str (first run) " - " (second run) "%")])]
       [:h3 "Corp"]
       [:ol
        (for [run (sort-by second > (om/get-state owner :corp-winrates))]
          [:li (str (first run) " - " (second run) "%")])]

       [:h2 "RNG settings"]
       [:p "CapAp starting hand agenda density: "
        (do (for [option [0 1 2 3 4 5]]
          [:label [:input {:type "radio"
                           :style {:margin-left "15px"}
                           :name "rng"
                           :value option
                           :checked (= (om/get-state owner [:rng :capap]) option)}]
           option]))]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:rng :snipe])}
            "Snipe only agenda during single HQ access"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:rng :siphon])}
            "Force Account Siphon in opening hand"]]

       [:h2 "Connection settings"]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :disconnect])}
            "Fake disconnect when losing"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :blame])}
            "Blame my terrible Internet connection on Weyland.meat"]]

       [:h2 "Latest decklists"]
       [:h4 "Dan D'Argenio"]
       [:ol
        [:li "For mtgred's eyes only."]]

       [:h4 "Chill84"]
       [:ol
        [:li "2x Account Siphon"]
        [:li "1x Apocalypse"]
        [:li "1x Yog.0"]
        [:li "1x Levy AR Lab Access"]
        [:li "43x Orange cards selected by your cat"]]


       [:h2 "Run Last Click safety word"]
       [:p [:a {:href "https://www.youtube.com/watch?v=-FaoCXTSUQc"} "PERSEPHONE"]]]))))

(om/root admin app-state {:target (. js/document (getElementById "admin"))})