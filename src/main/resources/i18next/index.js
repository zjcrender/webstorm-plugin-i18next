const fs = require('fs');
const path = require("path");

process.stdin.setEncoding('utf8');

function safeRequire(packageName, fallback = undefined) {
  try {
    return require(packageName);
  } catch (err) {
    console.log(err, packageName, fallback)
    return fallback;
  }
}

const server = {
  i18n: null,
  dir: null,

  setup(dir, lang, defaultNS) {
    const i18next = safeRequire(path.join(process.cwd(), 'node_modules', 'i18next'));
    if (!i18next) return "no i18next found"

    const resources = {}
    const namespaces = []

    fs.readdirSync(dir).forEach(name => {
      if (name !== lang) return

      const absPath = path.join(dir, name)
      const stats = fs.statSync(absPath)
      if (!stats.isDirectory()) return

      const resourceMap = {}
      fs.readdirSync(absPath).forEach(file => {
        if (!file.endsWith('.json')) return
        const ns = file.replace(/\.json$/, '')
        namespaces.push(ns)
        resourceMap[ns] = safeRequire(path.join(absPath, file))
      })
      resources[name] = resourceMap;
    })

    this.dir = dir;
    this.i18n = i18next.createInstance();
    this.i18n.init({
      resources,
      lng: lang,
      fallbackLng: lang,
      ns: namespaces,
      defaultNS,
    })
  },

  reload(ns) {
    if (!this.i18n) return

    this.i18n.addResourceBundle(
      this.i18n.language,
      ns,
      safeRequire(path.join(this.dir, this.i18n.language, `${ ns }.json`, Object.create(null))),
      true,
      true
    )
  },

  t(...args) {
    if (
      !this.i18n ||
      !this.i18n.exists(...args)
    ) return ""

    return `"${ this.i18n.t(...args) }"`
  }
}

process.stdin.on('data', (input) => {
  try {
    const { action, args, timestamp } = JSON.parse(input.trim());
    console.log({
      to: timestamp,
      data: server[action](...args)
    })
  } catch (e) {
    console.error(e);
  }
});


