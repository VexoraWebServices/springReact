# 18. Publishing (Maven Central)

The build is wired for Maven Central. Signing and the Central repository are **gated on
credentials**, so normal builds need no secrets — they only activate when you provide them.

## Artifacts produced

`./gradlew build` produces the main jar, a **sources** jar, and a **javadoc** jar (Central
requires all three). The POM already carries name, description, license, developer, and SCM.

## What you need (one-time)

1. A **Sonatype Central** account and a registered namespace for `com.vexora.springreact`.
2. A **GPG key** for signing (ASCII-armored private key + its passphrase).

## Publish

Provide these as environment variables, then publish:

```bash
export OSSRH_USERNAME="<sonatype-token-user>"
export OSSRH_PASSWORD="<sonatype-token-pass>"
export SIGNING_KEY="$(cat my-private-key.asc)"   # ASCII-armored
export SIGNING_PASSWORD="<gpg-passphrase>"

./gradlew publish        # signs + uploads to the Central staging repo
```

Then complete the release in the Sonatype Central UI (or via their API).

Without those variables, `publish` simply has no Central repository configured and signing
is skipped — handy for `publishToMavenLocal` during development:

```bash
./gradlew publishToMavenLocal   # installs com.vexora.springreact:SpringReact to ~/.m2
```

## The Gradle plugin

The `:gradle-plugin` module also publishes (`./gradlew :gradle-plugin:publishToMavenLocal`).
To list it on the **Gradle Plugin Portal**, add the `com.gradle.plugin-publish` plugin to
`gradle-plugin/build.gradle.kts` and run `publishPlugins` with your portal API keys.

## Note

The Central staging URL in `build.gradle.kts` may need adjusting to match your Sonatype
account type (legacy OSSRH vs the new Central portal). Everything else is in place.
