import "package:collection/collection.dart";
import "package:flutter/material.dart";
import "package:flutter/services.dart";
import "package:font_awesome_flutter/font_awesome_flutter.dart";
import "package:fuzzy/fuzzy.dart";
import "package:riverpod_annotation/riverpod_annotation.dart";
import "package:typewriter/models/adapter.dart";
import "package:typewriter/models/entry.dart";
import "package:typewriter/models/page.dart";
import "package:typewriter/pages/page_editor.dart";
import "package:typewriter/utils/extensions.dart";
import "package:typewriter/utils/passing_reference.dart";
import "package:typewriter/utils/smart_single_activator.dart";
import "package:typewriter/widgets/components/app/search_bar.dart";
import "package:typewriter/widgets/components/general/toasts.dart";
import "package:typewriter/widgets/inspector/inspector.dart";

part "entry_search.g.dart";

class TagFilter extends SearchFilter {
  const TagFilter(this.tag, {this.canRemove = true});

  final String tag;
  @override
  final bool canRemove;

  @override
  String get title => tag;
  @override
  Color get color => Colors.deepOrangeAccent;
  @override
  IconData get icon => FontAwesomeIcons.hashtag;

  @override
  bool filter(SearchElement action) {
    if (action is EntrySearchElement) {
      return action.blueprint.tags.contains(tag);
    }
    if (action is AddEntrySearchElement) {
      return action.blueprint.tags.contains(tag);
    }
    return true;
  }
}

class AddOnlyTagFilter extends TagFilter {
  const AddOnlyTagFilter(super.tag, {super.canRemove = true});

  @override
  bool filter(SearchElement action) {
    if (action is AddEntrySearchElement) {
      return action.blueprint.tags.contains(tag);
    }
    return true;
  }
}

class ExcludeEntryFilter extends SearchFilter {
  const ExcludeEntryFilter(this.entryId, {this.canRemove = true});

  final String entryId;
  @override
  final bool canRemove;

  @override
  String get title => "Exclude Entry";

  @override
  Color get color => Colors.orange;

  @override
  IconData get icon => FontAwesomeIcons.solidFileLines;

  @override
  bool filter(SearchElement action) {
    if (action is EntrySearchElement) {
      return action.entry.id != entryId;
    }
    return true;
  }
}

@riverpod
Fuzzy<EntryDefinition> _fuzzyEntries(_FuzzyEntriesRef ref) {
  final pages = ref.watch(pagesProvider);
  final definitions = pages.expand((page) {
    return page.entries.map((entry) {
      final blueprint = ref.watch(entryBlueprintProvider(entry.type));
      if (blueprint == null) return null;
      return EntryDefinition(
        pageId: page.name,
        blueprint: blueprint,
        entry: entry,
      );
    }).whereNotNull();
  }).toList();

  return Fuzzy(
    definitions,
    options: FuzzyOptions(
      threshold: 0.4,
      sortFn: (a, b) => a.matches.map((e) => e.score).sum.compareTo(b.matches.map((e) => e.score).sum),
      // tokenize: true,
      // verbose: true,
      keys: [
        // The names of entries are like "test.some_entry".
        // We want to give the last part more priority since it is more specific.
        WeightedKey(
          name: "name-suffix",
          getter: (definition) => definition.entry.name.split(".").last,
          weight: 0.4,
        ),
        WeightedKey(
          name: "name-full",
          getter: (definition) => definition.entry.name.formatted,
          weight: 0.15,
        ),
        WeightedKey(
          name: "type",
          getter: (definition) => definition.entry.type,
          weight: 0.4,
        ),
        WeightedKey(
          name: "tags",
          getter: (definition) => definition.blueprint.tags.join(" "),
          weight: 0.3,
        ),
        WeightedKey(
          name: "adapter",
          getter: (definition) => definition.blueprint.adapter,
          weight: 0.1,
        ),
      ],
    ),
  );
}

@riverpod
Fuzzy<EntryBlueprint> _fuzzyBlueprints(_FuzzyBlueprintsRef ref) {
  final blueprints = ref.watch(entryBlueprintsProvider);

  return Fuzzy(
    blueprints,
    options: FuzzyOptions(
      threshold: 0.3,
      sortFn: (a, b) => a.matches.map((e) => e.score).sum.compareTo(b.matches.map((e) => e.score).sum),
      keys: [
        WeightedKey(
          name: "name",
          getter: (blueprint) => "Add ${blueprint.name.formatted}",
          weight: 0.5,
        ),
        WeightedKey(
          name: "tags",
          getter: (blueprint) => blueprint.tags.join(" "),
          weight: 0.2,
        ),
        WeightedKey(
          name: "description",
          getter: (blueprint) => blueprint.description,
          weight: 0.4,
        ),
        WeightedKey(
          name: "adapter",
          getter: (blueprint) => blueprint.adapter,
          weight: 0.2,
        ),
      ],
    ),
  );
}

class NewEntryFetcher extends SearchFetcher {
  const NewEntryFetcher({
    this.onAdd,
    this.disabled = false,
  });

  final FutureOr<bool?> Function(EntryBlueprint)? onAdd;

  @override
  final bool disabled;

  @override
  String get title => "New Entries";

  @override
  List<SearchElement> fetch(PassingRef ref) {
    final search = ref.read(searchProvider);
    if (search == null) return [];
    final fuzzy = ref.read(_fuzzyBlueprintsProvider);

    final results = fuzzy.search(search.query);

    return results.map((result) => AddEntrySearchElement(result.item, onAdd: onAdd)).toList();
  }

  @override
  SearchFetcher copyWith({
    bool? disabled,
  }) {
    return NewEntryFetcher(
      onAdd: onAdd,
      disabled: disabled ?? this.disabled,
    );
  }
}

class EntryFetcher extends SearchFetcher {
  const EntryFetcher({
    this.onSelect,
    this.disabled = false,
  });

  final FutureOr<bool?> Function(Entry)? onSelect;

  @override
  final bool disabled;

  @override
  String get title => "Entries";

  @override
  List<SearchElement> fetch(PassingRef ref) {
    final search = ref.read(searchProvider);
    if (search == null) return [];
    final fuzzy = ref.read(_fuzzyEntriesProvider);

    final results = fuzzy.search(search.query);

    return results.map((result) {
      final definition = result.item;
      return EntrySearchElement(definition, onSelect: onSelect);
    }).toList();
  }

  @override
  SearchFetcher copyWith({
    bool? disabled,
  }) {
    return EntryFetcher(
      onSelect: onSelect,
      disabled: disabled ?? this.disabled,
    );
  }
}

extension SearchBuilderX on SearchBuilder {
  void tag(String tag, {bool canRemove = true}) {
    filter(TagFilter(tag, canRemove: canRemove));
  }

  void addOnlyTag(String tag, {bool canRemove = true}) {
    filter(AddOnlyTagFilter(tag, canRemove: canRemove));
  }

  void excludeEntry(String entryId, {bool canRemove = true}) {
    filter(ExcludeEntryFilter(entryId, canRemove: canRemove));
  }

  void fetchNewEntry({FutureOr<bool?> Function(EntryBlueprint)? onAdd}) {
    fetch(NewEntryFetcher(onAdd: onAdd));
  }

  void fetchEntry({FutureOr<bool?> Function(Entry)? onSelect}) {
    fetch(EntryFetcher(onSelect: onSelect));
  }
}

/// Action for selecting an existing entry.
class EntrySearchElement extends SearchElement {
  const EntrySearchElement(this.definition, {this.onSelect});
  final EntryDefinition definition;
  final FutureOr<bool?> Function(Entry)? onSelect;

  EntryBlueprint get blueprint => definition.blueprint;
  Entry get entry => definition.entry;

  @override
  String get title => entry.formattedName;

  @override
  Color color(BuildContext context) => blueprint.color;

  @override
  Widget icon(BuildContext context) => Icon(blueprint.icon);

  @override
  Widget suffixIcon(BuildContext context) => const Icon(FontAwesomeIcons.upRightFromSquare);

  @override
  String description(BuildContext context) => definition.pageId.formatted;

  @override
  List<SearchAction> actions(PassingRef ref) {
    return [
      const SearchAction(
        "Open",
        FontAwesomeIcons.upRightFromSquare,
        SingleActivator(LogicalKeyboardKey.enter),
      ),
      SearchAction(
        "Open Wiki",
        FontAwesomeIcons.book,
        SmartSingleActivator(LogicalKeyboardKey.keyO, control: true),
        onTrigger: (_, __) => blueprint.openWiki(),
      ),
    ];
  }

  @override
  Future<bool> activate(BuildContext context, PassingRef ref) async {
    if (onSelect != null) {
      return await onSelect?.call(entry) ?? true;
    }

    await ref.read(inspectingEntryIdProvider.notifier).navigateAndSelectEntry(ref, entry.id);
    return true;
  }
}

class AddEntrySearchElement extends SearchElement {
  const AddEntrySearchElement(this.blueprint, {this.onAdd});
  final EntryBlueprint blueprint;
  final FutureOr<bool?> Function(EntryBlueprint)? onAdd;

  @override
  String get title => "Add ${blueprint.name.formatted}";

  @override
  Color color(BuildContext context) => blueprint.color;

  @override
  Widget icon(BuildContext context) => Icon(blueprint.icon);

  @override
  Widget suffixIcon(BuildContext context) => const Icon(FontAwesomeIcons.plus);

  @override
  String description(BuildContext context) => blueprint.description;

  @override
  List<SearchAction> actions(PassingRef ref) {
    return [
      const SearchAction(
        "Add",
        FontAwesomeIcons.plus,
        SingleActivator(LogicalKeyboardKey.enter),
      ),
      SearchAction(
        "Open Wiki",
        FontAwesomeIcons.book,
        SmartSingleActivator(LogicalKeyboardKey.keyO, control: true),
        onTrigger: (_, __) => blueprint.openWiki(),
      ),
    ];
  }

  @override
  Future<bool> activate(BuildContext context, PassingRef ref) async {
    if (onAdd != null) {
      return await onAdd?.call(blueprint) ?? true;
    }
    final page = ref.read(currentPageProvider);
    if (page == null) return false;
    if (!page.canHave(blueprint)) {
      Toasts.showError(ref, "Could not create entry!", description: "Page does not support this  of entry.");
      return false;
    }
    final entry = await page.createEntryFromBlueprint(ref, blueprint);
    await ref.read(inspectingEntryIdProvider.notifier).navigateAndSelectEntry(ref, entry.id);
    return true;
  }
}
