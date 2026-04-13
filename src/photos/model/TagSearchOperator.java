package photos.model;

/**
 * Lists the operators used when searching by tags.
 * The user can search with one tag, or connect two tags with AND or OR.
 *
 * @author Owner
 */
public enum TagSearchOperator {
    SINGLE,
    AND,
    OR
}
