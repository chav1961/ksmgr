package chav1961.ksmgr;

class ApplicationMenuManager {
	static final String		MENU_FILE_NEW = "menu.file.newkeystore";
	static final String		MENU_FILE_OPEN = "menu.file.openkeystore";
	static final String		MENU_FILE_LRU = "menu.file.lru";
	static final String		MENU_FILE_SAVE = "menu.file.savekeystore";
	static final String		MENU_FILE_SAVE_AS = "menu.file.savekeystoreas";
	static final String		MENU_FILE_CLOSE = "menu.file.closekeystore";
	static final String		MENU_FILE_CHANGE_PASSWD = "menu.file.changePassword";

	static final String		MENU_EDIT = "menu.edit";
	static final String		MENU_EDIT_COPY = "menu.edit.copy";
	static final String		MENU_EDIT_MOVE = "menu.edit.move";
	static final String		MENU_EDIT_RENAME = "menu.edit.rename";
	static final String		MENU_EDIT_DELETE = "menu.edit.delete";
	
	static final String		MENU_TASKS = "menu.tasks";
	static final String		MENU_TASKS_KEYPAIRS = "menu.tasks.keypairs";
	static final String		MENU_TASKS_CERTIFICATES = "menu.tasks.certificates";
	static final String		MENU_TASKS_KEYS = "menu.tasks.keys";
	static final String		MENU_TASKS_CRYPTO = "menu.tasks.crypto";
	
	static final String[]	MENUS = {
								MENU_FILE_NEW,
								MENU_FILE_OPEN,
								MENU_FILE_LRU,
								MENU_FILE_SAVE,
								MENU_FILE_SAVE_AS,
								MENU_FILE_CLOSE,
								MENU_FILE_CHANGE_PASSWD,
								
								MENU_EDIT,
								MENU_EDIT_COPY,
								MENU_EDIT_MOVE,
								MENU_EDIT_RENAME,
								MENU_EDIT_DELETE,

								MENU_TASKS,
								MENU_TASKS_KEYPAIRS,
								MENU_TASKS_CERTIFICATES,
								MENU_TASKS_KEYS,
								MENU_TASKS_CRYPTO,
							};

	static final long		FILE_NEW = 1L << 0;
	static final long		FILE_OPEN = 1L << 1;
	static final long		FILE_LRU = 1L << 2;
	static final long		FILE_SAVE = 1L << 3;
	static final long		FILE_SAVE_AS = 1L << 4;
	static final long		FILE_CLOSE = 1L << 5;
	static final long		FILE_CHANGE_PASSWD = 1L << 6;

	static final long		EDIT = 1L << 7;
	static final long		EDIT_COPY = 1L << 8;
	static final long		EDIT_MOVE = 1L << 9;
	static final long		EDIT_RENAME = 1L << 10;
	static final long		EDIT_DELETE = 1L << 11;
	
	static final long		TASKS = 1L << 12;
	static final long		TASKS_KEYPAIRS = 1L << 13;
	static final long		TASKS_CERTIFICATES = 1L << 14;
	static final long		TASKS_KEYS = 1L << 15;
	static final long		TASKS_CRYPTO = 1L << 16;
}
