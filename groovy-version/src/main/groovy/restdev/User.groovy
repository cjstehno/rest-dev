package restdev

import groovy.transform.Canonical

@Canonical
class User {

    Long id
    String username
    String email
}
