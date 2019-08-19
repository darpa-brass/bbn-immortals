Tree hashing comparison
===================================


The problem
-------------

Compare two XSD specs with a tree of fields in a performatic way. Understanding a tree as the relation of nodes and it's children nodes in a hierarchical way.


Solution using tree hashing
--------------------------------------

### Tree hashing calculation

The main idea here is to calculate a hash of every node in the tree. For each node, it's hash is calculated considering the following formula:

```
node_hash = hash(hash("[node name]-[node type name]"), "-", hash(node_type_hash))
```

Where `node_type_hash` is calculated with:

```
node_type_hash = hash(hash("[node type name]"), hash(child1), hash(child2), hash(chlid3), ... ,hash(childN))
```

Where `childN` is another node following `node_hash` formula. As you can see, it's a recursion that iterates over all tree.

As it's a recursion, its stop condition is a node that has no children (like a `xsd:string` node, for e.g.).

Bellow, a diagram explaining hashing calculation for a very simple tree.

![image](https://user-images.githubusercontent.com/756802/60775237-677aa280-a0f6-11e9-9d47-4bfbd0385ec2.png)

We can see that `Network` node has a hash `55555`, that was calculated based on self attributes and hashes of `Owner`, `Name` and `RouteMetric`. The node `MDLRoot` has its hash calculated using self attributes and hash of `Owner` and `Network` nodes.

It's important to see that nodes with same name and children will have the same hash.

We can see bellow an example of a practical tree with it's hashes:

```
> MDLRoot-MDLRootType -2806790678738728711
--> Checksum-xsd:string 1759760783633701748
--> ConfigurationVersion-xsd:string -7620894309812646021
--> DatabaseID-xsd:string 5084057480071972130
--> DirtyBit-xsd:boolean 348786821432222469
--> NetworkDomains-NetworkDomainsType 5139398322643518822
----> Network-NetworkType 6489740139883723251
------> Description-xsd:string -6603916454123007424
------> Name-xsd:token -3318243132111773253
------> NetworkNode-NetworkNodeType -4454423591871292059
--------> Description-xsd:string -6603916454123007424
--------> Name-xsd:token -3318243132111773253
--------> NetworkName-xsd:token 3006012143715379877
--------> Owner-xsd:token 2061782090809342339
--------> ReadOnly-xsd:boolean 1823591753307194139
--------> Routes-RoutesType -6884571706902032091
----------> Owner-xsd:token 2061782090809342339
----------> ReadOnly-xsd:boolean 1823591753307194139
----------> Route-RouteType 2245275050930069999
------------> Metric-xsd:positiveInteger 5992748253419076062
------------> Owner-xsd:token 2061782090809342339
------------> ReadOnly-xsd:boolean 1823591753307194139
------> Owner-xsd:token 2061782090809342339
------> ReadOnly-xsd:boolean 1823591753307194139
----> Owner-xsd:token 2061782090809342339
----> ReadOnly-xsd:boolean 1823591753307194139
--> Owner-xsd:token 2061782090809342339
--> ReadOnly-xsd:boolean 1823591753307194139
```

As you can see, simple elements as `Owner` and `ReadOnly` has the same hash. More complex elements has it's hashes build using self attrs and children hashes.

### Tree hashing comparison

Now, we have in root node a hash that was calculated using all tree bellow it. So, if we compare two equal trees we'll have the same hash in root node, spending a `O(1)` time to compare two trees in this situation.

If we have just a field renaming, both root nodes of trees will have different hashes, so we just need to search deeply in the tree following nodes that has different hashes.

Here is a example of renaming node `/MDLRoot/NetworkDomains/Network/NetworkNode/Owner` to `/MDLRoot/NetworkDomains/Network/NetworkNode/NewOwner`, pay attention that only this path has different hashes between trees, the other fields that was not touched has the same hash (like `/MDLRoot/NetworkDomains/Network/NetworkNode/Routes`):


```
> MDLRoot-MDLRootType 7506255522858997719
--> Checksum-xsd:string -2425188790865716594
--> ConfigurationVersion-xsd:string -8583165367627013929
--> DatabaseID-xsd:string -5757739491427376014
--> DirtyBit-xsd:boolean -6256753327797116070
--> NetworkDomains-NetworkDomainsType 6330079046187466217
----> Network-NetworkType 3588755134704803191
------> Description-xsd:string -2239705521625426001
------> Name-xsd:token 230514973621263928
------> NetworkNode-NetworkNodeType 3192345578454755879
--------> Description-xsd:string -2239705521625426001
--------> Name-xsd:token 230514973621263928
--------> NetworkName-xsd:token 4333467257559391491
--------> Owner-xsd:token -8475282241866750629
--------> ReadOnly-xsd:boolean 108364062556523071
--------> Routes-RoutesType -6055162102963863434
----------> Owner-xsd:token -8475282241866750629
----------> ReadOnly-xsd:boolean 108364062556523071
----------> Route-RouteType 7792328412385497020
------------> Metric-xsd:positiveInteger -5421162724478418312
------------> Owner-xsd:token -8475282241866750629
------------> ReadOnly-xsd:boolean 108364062556523071
------> Owner-xsd:token -8475282241866750629
------> ReadOnly-xsd:boolean 108364062556523071
----> Owner-xsd:token -8475282241866750629
----> ReadOnly-xsd:boolean 108364062556523071
--> Owner-xsd:token -8475282241866750629
--> ReadOnly-xsd:boolean 108364062556523071
```

The second tree:

```
> MDLRoot-MDLRootType -621190640461853872
--> Checksum-xsd:string -2425188790865716594
--> ConfigurationVersion-xsd:string -8583165367627013929
--> DatabaseID-xsd:string -5757739491427376014
--> DirtyBit-xsd:boolean -6256753327797116070
--> NetworkDomains-NetworkDomainsType -4762600688912887010
----> Network-NetworkType 8004903950545617896
------> Description-xsd:string -2239705521625426001
------> Name-xsd:token 230514973621263928
------> NetworkNode-NetworkNodeType 1371433077490304621
--------> Description-xsd:string -2239705521625426001
--------> Name-xsd:token 230514973621263928
--------> NetworkName-xsd:token 4333467257559391491
--------> NewOwner-xsd:token -8084258739714809417
--------> ReadOnly-xsd:boolean 108364062556523071
--------> Routes-RoutesType -6055162102963863434
----------> Owner-xsd:token -8475282241866750629
----------> ReadOnly-xsd:boolean 108364062556523071
----------> Route-RouteType 7792328412385497020
------------> Metric-xsd:positiveInteger -5421162724478418312
------------> Owner-xsd:token -8475282241866750629
------------> ReadOnly-xsd:boolean 108364062556523071
------> Owner-xsd:token -8475282241866750629
------> ReadOnly-xsd:boolean 108364062556523071
----> Owner-xsd:token -8475282241866750629
----> ReadOnly-xsd:boolean 108364062556523071
--> Owner-xsd:token -8475282241866750629
--> ReadOnly-xsd:boolean 108364062556523071
```

This was just an example using renaming operation, we can use this tree hashing to calculatate all operations, like additions, relocations (a node that was moved to another place), etc.
