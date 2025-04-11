#!/bin/bash
set -uex

prefix=harbor.k8s.elab.rs/banka-4/

: "${version:=$(git rev-parse HEAD)}"

java_services=(
    {notification,user,stock}-service
)

for service in "${java_services[@]}"; do
    docker build \
           -t "${prefix}${service}":"${version}" \
           -f docker/Dockerfile-build-java \
           --build-arg SERVICE="${service}" \
           .
done

docker build \
       -f docker/Dockerfile-build-exchange-office \
       -t "${prefix}exchange-office":"${version}" \
       .

if [[ ${1-} ]]; then
    # Call with an argument to get a push
    for service in "${java_services[@]}" exchange-office; do
        docker push "${prefix}${service}":"${version}"
        docker tag \
               "${prefix}${service}":"${version}" \
               "${prefix}${service}":latest
        docker push "${prefix}${service}":latest
    done
fi

# Local Variables:
# indent-tabs-mode: nil
# End:
