#!/usr/bin/env bash
# create-springreact — scaffold a new SpringReact app (Kotlin + Spring Boot, no frontend).
#
# Usage:
#   ./create-springreact.sh <app-name> [package] [springreact-version]
#
# Example:
#   ./create-springreact.sh my-app com.acme.myapp 0.1.0
set -euo pipefail

APP="${1:-}"
PKG="${2:-com.example.app}"
VERSION="${3:-0.1.0}"

if [[ -z "$APP" ]]; then
  echo "usage: create-springreact.sh <app-name> [package] [springreact-version]" >&2
  exit 1
fi
if [[ -e "$APP" ]]; then
  echo "error: '$APP' already exists" >&2
  exit 1
fi

PKG_PATH="${PKG//.//}"
SRC="$APP/src/main/kotlin/$PKG_PATH"
mkdir -p "$SRC"

cat > "$APP/settings.gradle.kts" <<EOF
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "$APP"
EOF

cat > "$APP/build.gradle.kts" <<EOF
plugins {
    id("com.vexora.springreact") version "$VERSION"
}

group = "$PKG"
version = "0.0.1"

springReact {
    version.set("$VERSION")
    javaVersion.set(21)
}
EOF

cat > "$SRC/App.kt" <<EOF
package $PKG

import com.vexora.springreact.jsc.Html.button
import com.vexora.springreact.jsc.Html.cls
import com.vexora.springreact.jsc.Html.div
import com.vexora.springreact.jsc.Html.h1
import com.vexora.springreact.jsc.Html.onClick
import com.vexora.springreact.jsc.ServerComponent
import com.vexora.springreact.jsc.UiNode
import com.vexora.springreact.live.LiveAction
import com.vexora.springreact.live.LiveComponent
import com.vexora.springreact.live.LiveState
import com.vexora.springreact.web.Route
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@LiveComponent("Home")
@Route("/")
class HomeScreen : ServerComponent {
    @LiveState var count = 0
    @LiveAction fun inc() { count++ }

    override fun render(): UiNode =
        div(cls("card"), h1("Welcome to $APP 👋"), button(onClick("inc"), "Count: \$count"))
}
EOF

cat > "$APP/README.md" <<EOF
# $APP

A SpringReact app. Run it:

\`\`\`bash
./gradlew bootRun   # or: gradle bootRun
\`\`\`

Open http://localhost:8080. Add screens in \`src/main/kotlin/$PKG_PATH\`.
EOF

# Generate the Gradle wrapper if a gradle is available (so ./gradlew works out of the box).
if command -v gradle >/dev/null 2>&1; then
  (cd "$APP" && gradle wrapper --quiet >/dev/null 2>&1 || true)
fi

echo "Created '$APP'."
echo "Next:"
echo "  cd $APP"
echo "  ./gradlew bootRun     # then open http://localhost:8080"
