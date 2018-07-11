#!/usr/bin/env bash

mkdir -p knowledge-repo/vocabulary/ontology-vocab-domains/cp/src/main/java/mil/darpa/immortals/ontology

mv shared/modules/core/src/main/java/mil/darpa/immortals/ontology/CryptoFunctionalitySpec.java knowledge-repo/vocabulary/ontology-vocab-domains/cp/src/main/java/mil/darpa/immortals/ontology/

mv shared/modules/core/src/main/java/mil/darpa/immortals/ontology/BaselineFunctionalitySpec.java knowledge-repo/vocabulary/ontology-vocab-domains/cp/src/main/java/mil/darpa/immortals/ontology/

mkdir -p knowledge-repo/vocabulary/ontology-vocab-domains/sa/src/main/java/mil/darpa/immortals/ontology/

mv shared/modules/core/src/main/java/mil/darpa/immortals/ontology/CryptoFunctionalAspect.java knowledge-repo/vocabulary/ontology-vocab-domains/sa/src/main/java/mil/darpa/immortals/ontology/

mv shared/modules/core/src/main/java/mil/darpa/immortals/ontology/BaselineFunctionalAspect.java knowledge-repo/vocabulary/ontology-vocab-domains/sa/src/main/java/mil/darpa/immortals/ontology/


