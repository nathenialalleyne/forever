# Synthetic command observations
function matcha:nested/helper
schedule function matcha:later 1s replace
scoreboard objectives add matcha_points dummy
scoreboard players set @s matcha_points 1
execute if score @s matcha_points matches 1 run function minecraft:tick
advancement grant @s only matcha:adv
advancement revoke @s only minecraft:story/root
recipe give @s matcha:custom
recipe take @s minecraft:vanilla
loot give @s loot matcha:loot
item modify entity @s weapon.mainhand matcha:modifier
data modify storage matcha:state value set value {value:1b}
