# Hopper Item Filter plugin

### Adds a new item, which can be applied onto a hopper, to turn it into a "filter hopper". It can be configured to filter 27 different types of blocks. 

New item is added:

![recipe](https://github.com/user-attachments/assets/ce236dd7-0db9-4732-9843-de648a09e2f2)

You get that recipe unlocked the moment you discover redstone, and its recipes (all redstone components):

![recipe_unlocked](https://github.com/user-attachments/assets/4cb14674-b36c-42f0-8a3e-fddf62675943)

You can apply filter onto a hopper to make it appear as "filter hopper":

![filter_hopper](https://github.com/user-attachments/assets/0a6ddd5d-747a-437a-ae7b-ef9cd45fd00b)

The display of this is done using Block Display entity, it doesn't require custom datapacks.

You can setup a typical line of hoppers, with your filter hoppers underneath to grab any set of items you want to place into a chest:

![filter_hopper_in_action](https://github.com/user-attachments/assets/051741c4-f812-404c-b388-7a296d9283ca)

If works the same way as a normal hopper would do - it transports one item per tick, has the same transport delays etc. Internally it doesn't modify any of the hopper mechanics as the filtering is done by blocking/unblocking hopper slots in an event preceeding item movement.
It has only one major difference: it will not accept an item into a hopper if the "target" destination of a filter hopper cannot receive an item. The idea is that filter hoppers should never hold the items they filter - the items either are transported to the "target" or not taken from the "source" of the filter hopper.

In case the chest/hopper/other inventory holder that the filter hopper points to is full, the filter hopper indicates that with strip of a soul fire animation on its ring. This will draw player's attention to an overflowing chest.

![blocked_filter_hopper](https://github.com/user-attachments/assets/7170150f-efa0-4b0b-ae81-769fe19d329d)

You can set up your filter with any items. In the example below it will "pass" a couple different oak wood blocks:

![setting_up_filter_items](https://github.com/user-attachments/assets/5fd06a9c-033e-4938-97b4-255c7650bc91)

The check for "accepting" and item is done using "material" type, which means if you put a typical diamond sword in there, it will accept any other diamond sword - no matter the name, enchantments etc.

I threw a bunch of different wood items into the first chest, and I got this in the chest below the filter hopper:

![filtered_items](https://github.com/user-attachments/assets/f55d4b03-11f8-40e0-b832-afeef574e538)

And this at the last chest:
![unfiltered_items](https://github.com/user-attachments/assets/55825184-70c7-429c-a3bf-58eb7995404d)

