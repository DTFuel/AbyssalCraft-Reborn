const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const BOOKS = [
    ['necronomicon', 229],
    ['abyssal_wasteland_necronomicon', 300],
    ['dreadlands_necronomicon', 363],
    ['omothol_necronomicon', 421],
    ['abyssalnomicon', 444],
];
const LANGUAGES = ['en_us', 'es_es', 'fr_fr', 'ja_jp', 'ko_kr', 'ru_ru', 'zh_cn', 'zh_tw'];
const LANG_ROOT = path.join(ROOT, 'src/main/resources/assets/abyssalcraft/lang');
const BOOK_ROOT = path.join(ROOT, 'src/main/generated/assets/abyssalcraft/patchouli_books');

function walk(directory) {
    const result = [];
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
        const file = path.join(directory, entry.name);
        if (entry.isDirectory()) result.push(...walk(file));
        else if (entry.name.endsWith('.json')) result.push(file);
    }
    return result;
}

const failures = [];
let entryViews = 0;
for (const language of LANGUAGES) {
    const translations = JSON.parse(fs.readFileSync(path.join(LANG_ROOT, `${language}.json`), 'utf8'));
    for (const [book, expectedCount] of BOOKS) {
        const entryRoot = path.join(BOOK_ROOT, book, 'en_us', 'entries');
        const entries = walk(entryRoot).map(file => {
            const json = JSON.parse(fs.readFileSync(file, 'utf8'));
            return {
                file: path.relative(entryRoot, file),
                key: json.name,
                value: translations[json.name],
            };
        });
        entryViews += entries.length;
        if (entries.length !== expectedCount) {
            failures.push(`${language}/${book} entries=${entries.length}, expected=${expectedCount}`);
        }
        for (const entry of entries.filter(entry => entry.value === undefined)) {
            failures.push(`${language}/${book} missing ${entry.key} for ${entry.file}`);
        }
        const byValue = new Map();
        for (const entry of entries) {
            if (entry.value === undefined) continue;
            const group = byValue.get(entry.value) || [];
            group.push(entry.file);
            byValue.set(entry.value, group);
        }
        for (const [value, files] of byValue) {
            if (files.length > 1) {
                failures.push(`${language}/${book} duplicate ${JSON.stringify(value)}: ${files.join(', ')}`);
            }
        }
    }
}

if (failures.length) {
    throw new Error(`RR_NECRO_TITLE_AUDIT_FAIL count=${failures.length}\n${failures.join('\n')}`);
}
console.log(`RR_NECRO_TITLE_AUDIT_OK books=${BOOKS.length} languages=${LANGUAGES.length}`
    + ` entryViews=${entryViews} missing=0 duplicateValues=0`);