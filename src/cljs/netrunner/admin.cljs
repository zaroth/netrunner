(ns netrunner.admin
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [netrunner.ajax :refer [GET]]))

(defn- new-state []
  {:runner-winrates [["Whizzard: Master Gamer" (+ 80 (rand 10))]
                     ["MaxX: Maximum Punk Rock" (+ 75 (rand 6))]
                     ["Valencia Estevez: The Angel of Cayambe" (+ 70 (rand 5))]
                     ["Noise: Hacker Extraordinaire" (+ 68 (rand 3))]
                     ["The Professor: Keeper of Knowledge" (+ 50 (rand 15))]]
   :corp-winrates [["Haas-Bioroid: Engineering the Future" (+ 60 (rand 10))]
                    ["Cerebral Imaging: Infinite Frontiers" 7]]
   :capap (rand-int 6)
   :editor (first (shuffle ["mtgred" "nealterrell" "Saintis" "zaroth" "JoelCFC25" "queueseven"
                            "mediohxcore" "d1en's cat" "Apex"]))})

(def app-state (atom (new-state)))

(defn admin [cursor owner]
  (reify
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
        (for [run (:runner-winrates cursor)]
          [:li (str (first run) " - " (second run) "%")])]
       [:h3 "Corp"]
       [:ol
        (for [run (:corp-winrates cursor)]
          [:li (str (first run) " - " (second run) "%")])]

       [:h2 "RNG settings"]
       [:p "CapAp starting hand agenda density: "
        (do (for [option [0 1 2 3 4 5]]
          [:label [:input {:type "radio"
                           :style {:margin-left "15px"}
                           :name "rng"
                           :value option
                           :checked (= (om/get-state owner :capap) option)}]
           option]))]
       [:p [:input {:disabled true :type "checkbox"} "Snipe only agenda during single HQ access"]]
       [:p [:input {:disabled true :type "checkbox"} "Force Account Siphon in opening hand"]]

       [:h2 "Connection settings"]
       [:p [:input {:disabled true :type "checkbox"} "Fake disconnect when losing"]]
       [:p [:input {:disabled true :type "checkbox"} "Blame my terrible Internet connection on Weyland.meat"]]


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