package com.laptevn;

public final class ErrorMessages {
    public final static String EMPTY_NAME = "Name cannot be empty";
    public final static String EMPTY_PASSWORD = "Password cannot be empty";
    public final static String NEW_NAME_IS_USED = "New name is already in use";
    public final static String INVALID_PAGE_INDEX = "Page index should start at 1";
    public final static String INVALID_PAGE_SIZE = "Page size cannot be less than one";
    public final static String USER_ALREADY_EXISTS = "User already exists";
    public final static String UPDATE_USER_AMBIGUOUS_NAME = "Cannot create new user. Ambiguous user names were provided.";
    public final static String USER_WAS_NOT_FOUND_TEMPLATE = "User with name=%s was not found";
    public final static String INVALID_FORMAT_WHERE = "Invalid format of where clause expression";
    public final static String INVALID_FORMAT_WHERE_DETAILS = INVALID_FORMAT_WHERE + ". Details: ";
    public final static String NOT_SUPPORTED_DATA_TYPE_FORMAT = "'%s' data type is not supported";
    public final static String NOT_EXISTING_ROLE_FORMAT = "'%s' role doesn't exist";
    public final static String INCOMPATIBLE_VALUE_TYPE = "Value type doesn't correspond to field data type";
    public final static String EMPTY_LOCATION = "Location cannot be empty";
    public final static String NOT_EXISTING_USER_FORMAT = "'%s' user doesn't exist";
    public final static String INVALID_USER = "Invalid format of user name. It cannot be used in URI.";
}