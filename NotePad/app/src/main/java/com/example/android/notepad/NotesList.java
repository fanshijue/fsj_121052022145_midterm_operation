package com.example.android.notepad;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotesList extends ListActivity {

    // For logging and debugging
    private static final String TAG = "NotesList";

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_NOTE, //2
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 3
            NotePad.Notes.COLUMN_NAME_BACK_COLOR //4
    };

    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_NOTE = 2;
    private static final int COLUMN_INDEX_MODIFICATION_DATE = 3;
    private  static  final  int COLUMN_INDEX_BACK_COLOR = 4;

    private ListView originalListView;
    private ListView searchResultsListView;

    private SimpleCursorAdapter mAdapter;
    private SimpleCursorAdapter mSearchAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_list);

        // Initialize the new ListView for search results
        searchResultsListView = findViewById(R.id.search_results_list);
        originalListView = getListView();

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        Intent intent = getIntent();

        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        getListView().setOnCreateContextMenuListener(this);

        Cursor cursor = managedQuery(
            getIntent().getData(),            // Use the default content URI for the provider.
            PROJECTION,                       // Return the note ID and title for each note.
            null,                             // No where clause, return all records.
            null,                             // No where clause, therefore no where column values.
            NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
        );

        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };

        int[] viewIDs = { android.R.id.text1, R.id.text2 };

        mAdapter
                = new SimpleCursorAdapter(
                this,                             // The Context for the ListView
                R.layout.noteslist_item,          // Points to the XML for a list item
                cursor,                           // The cursor to get items from
                dataColumns,
                viewIDs
        );
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                int x = cursor.getInt(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
                switch (x){
                    case NotePad.Notes.WHITE_COLOR:
                        view.setBackgroundColor(getResources().getColor(R.color.background_white));
                        break;
                    case NotePad.Notes.RED_COLOR:
                        view.setBackgroundColor(getResources().getColor(R.color.background_red));
                        break;
                    case NotePad.Notes.BLUE_COLOR:
                        view.setBackgroundColor(getResources().getColor(R.color.background_blue));
                        break;
                    case NotePad.Notes.YELLOW_COLOR:
                        view.setBackgroundColor(getResources().getColor(R.color.background_yellow));
                        break;
                    case NotePad.Notes.GREEN_COLOR:
                        view.setBackgroundColor(getResources().getColor(R.color.background_green));
                        break;
                    case NotePad.Notes.PURPLE_COLOR:
                        view.setBackgroundColor(getResources().getColor(R.color.background_purple));
                        break;
                    default:
                        view.setBackgroundColor(getResources().getColor(R.color.background_white));
                        break;
                }
                if (columnIndex == COLUMN_INDEX_MODIFICATION_DATE) {
                    long timestamp = cursor.getLong(columnIndex);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));//修改时区为东八区
                    String formattedDate = dateFormat.format(new Date(timestamp));
                    ((TextView) view).setText(formattedDate);
                    return true;
                }
                return false;
            }
        });

        // 设置searchResultsListView的点击事件
        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(searchResultsListView, view, position, id);
            }
        });

        // 设置searchResultsListView的长按事件以显示上下文菜单
        searchResultsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 创建AdapterContextMenuInfo对象
                AdapterView.AdapterContextMenuInfo info = new AdapterView.AdapterContextMenuInfo(view, position, id);
                // 显示上下文菜单
                registerForContextMenu(searchResultsListView);
                // 触发上下文菜单的创建
                openContextMenu(searchResultsListView);
                return true;
            }
        });


        setListAdapter(mAdapter);
    }

    private void createNote() {
        // 隐藏搜索结果并显示所有笔记
        showAllNotes();
        // 启动添加笔记的活动
        Intent intent = new Intent(Intent.ACTION_INSERT, getIntent().getData());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem addItem = menu.add(0, R.id.menu_add, 0, R.string.menu_add);
        addItem.setIcon(android.R.drawable.ic_menu_add);
        addItem.setShortcut('3', 'a');

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 当用户提交查询时调用
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);


        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            mPasteItem.setEnabled(false);
        }

        final boolean haveItems = getListAdapter().getCount() > 0;

        if (haveItems) {

            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            Intent[] specifics = new Intent[1];

            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            MenuItem[] items = new MenuItem[1];

            Intent intent = new Intent(null, uri);

            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            menu.addIntentOptions(
                Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
                Menu.NONE,                  // A unique item ID is not required.
                Menu.NONE,                  // The alternatives don't need to be in order.
                null,                       // The caller's name is not excluded from the group.
                specifics,                  // These specific options must appear first.
                intent,                     // These Intent objects map to the options in specifics.
                Menu.NONE,                  // No flags are required.
                items                       // The menu items generated from the specifics-to-
                                            // Intents mapping
            );
                if (items[0] != null) {
                    items[0].setShortcut('1', 'e');
                }
            } else {
                menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
            }

        // Displays the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
            createNote();
            return true;

        case R.id.menu_reload:
            showAllNotes();
            return true;

        case R.id.menu_paste:
            startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
            return true;

        case R.id.menu_filter_by_color:
            showColorFilterDialog();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        long modificationDate = cursor.getLong(COLUMN_INDEX_MODIFICATION_DATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));//修改时区为东八区
        String formattedDate = dateFormat.format(new Date(modificationDate));


        if (cursor == null) {
            return;
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE) + " - " + formattedDate);

        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                                        Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        switch (item.getItemId()) {

        case R.id.context_open:
            startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
            return true;

        case R.id.context_copy:
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);

            clipboard.setPrimaryClip(ClipData.newUri(   // new clipboard item holding a URI
                    getContentResolver(),               // resolver to retrieve URI info
                    "Note",                             // label for the clip
                    noteUri)                            // the URI
            );
            return true;

        case R.id.context_delete:
            getContentResolver().delete(
                noteUri,  // The URI of the provider
                null,     // No where clause is needed, since only a single note ID is being
                          // passed in.
                null      // No where clause is used, so no where arguments are needed.
            );
            return true;

        case R.id.context_export:
            exportNotes(noteUri);
            return true;

        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // 检查当前ListView是否为搜索结果列表
        if (searchResultsListView.getVisibility() == View.VISIBLE) {
            // 如果是搜索结果列表，则从搜索适配器获取笔记ID
            Cursor cursor = (Cursor) mSearchAdapter.getItem(position);
            if (cursor != null) {
                int noteIdColumnIndex = cursor.getColumnIndex(NotePad.Notes._ID);
                if (noteIdColumnIndex >= 0) {
                    long noteId = cursor.getLong(noteIdColumnIndex);
                    Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId);
                    Intent intent = new Intent(Intent.ACTION_EDIT, uri);
                    startActivity(intent);
                } else {
                    // 如果_ID列不存在，显示错误消息
                    Toast.makeText(this, "无法找到笔记ID", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // 如果当前ListView不是搜索结果列表，则直接使用传入的id
            Uri uri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id);
            Intent intent = new Intent(Intent.ACTION_EDIT, uri);
            startActivity(intent);
        }
    }



    private void performSearch(String query) {


        // 在开始新的搜索之前，清除适配器中的数据
        if (mSearchAdapter != null) {
            mSearchAdapter.swapCursor(null);
        }

        // 重启Loader来执行新的查询
        getLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
                String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
                return new CursorLoader(
                        NotesList.this,
                        getIntent().getData(),
                        PROJECTION,
                        selection,
                        selectionArgs,
                        NotePad.Notes.DEFAULT_SORT_ORDER
                );
            }


            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                // 在这里，我们使用新的查询结果来更新适配器
                if (cursor != null && cursor.getCount() > 0) {
                    if (mSearchAdapter == null) {
                        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
                        int[] viewIDs = { android.R.id.text1, R.id.text2 };
                        mSearchAdapter = new SimpleCursorAdapter(
                                NotesList.this,                             // The Context for the ListView
                                R.layout.noteslist_item,                    // Points to the XML for a list item
                                cursor,                                     // The cursor to get items from
                                dataColumns,
                                viewIDs,
                                0
                        );
                        mSearchAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                            @Override
                            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                                int x = cursor.getInt(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
                                switch (x){
                                    case NotePad.Notes.WHITE_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_white));
                                        break;
                                    case NotePad.Notes.RED_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_red));
                                        break;
                                    case NotePad.Notes.BLUE_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_blue));
                                        break;
                                    case NotePad.Notes.YELLOW_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_yellow));
                                        break;
                                    case NotePad.Notes.GREEN_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_green));
                                        break;
                                    case NotePad.Notes.PURPLE_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_purple));
                                        break;
                                    default:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_white));
                                        break;
                                }
                                if (columnIndex == COLUMN_INDEX_MODIFICATION_DATE) {
                                    long timestamp = cursor.getLong(columnIndex);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00")); // 修改时区为东八区
                                    String formattedDate = dateFormat.format(new Date(timestamp));
                                    ((TextView) view).setText(formattedDate);
                                    return true;
                                }
                                return false;
                            }
                        });
                        searchResultsListView.setAdapter(mSearchAdapter); // 设置搜索结果的适配器
                    } else {
                        mSearchAdapter.swapCursor(cursor); // 使用新的游标更新适配器
                    }
                    originalListView.setVisibility(View.GONE); // 隐藏原始 ListView
                    searchResultsListView.setVisibility(View.VISIBLE);
                } else {
                    // 如果没有搜索结果，显示所有笔记并提示用户
                    Toast.makeText(NotesList.this, "没有找到对应的笔记标题或内容", Toast.LENGTH_SHORT).show();
                    showAllNotes();
                }
            }


            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // 当 Loader 重置时，清除适配器的数据
                if (mSearchAdapter != null) {
                    mSearchAdapter.swapCursor(null);
                }
            }
        });
    }


    private void showAllNotes() {
        // 显示所有笔记的代码
        originalListView.setVisibility(View.VISIBLE);
        searchResultsListView.setVisibility(View.GONE);
        // 重新设置适配器以显示所有笔记
        setListAdapter(mAdapter);
    }

    private void showColorFilterDialog() {
        final String[] colors = getResources().getStringArray(R.array.color_filter_options);
        final int[] colorDrawables = {
                R.drawable.background_white,
                R.drawable.background_red,
                R.drawable.background_blue,
                R.drawable.background_yellow,
                R.drawable.background_green,
                R.drawable.background_purple
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, colors) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.context_color_item, parent, false);
                    holder = new ViewHolder();
                    holder.imageView = convertView.findViewById(R.id.color_icon);
                    holder.textView = convertView.findViewById(R.id.color_text);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.imageView.setImageResource(colorDrawables[position]);
                holder.textView.setText(colors[position]); // 假设 colorNames 是一个包含颜色名称的字符串数组
                return convertView;
            }

            class ViewHolder {
                ImageView imageView;
                TextView textView;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_filter_by_color)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        searchResultsListView.setVisibility(View.GONE); // 隐藏搜索结果ListView
                        originalListView.setVisibility(View.VISIBLE); // 显示原始ListView
                        setListAdapter(mAdapter); // 确保使用的是mAdapter
                        int colorValue = getColorValueForIndex(which);
                        filterNotesByColor(colorValue);
                    }
                })
                .show();
    }

    private int getColorValueForIndex(int index) {
        switch (index) {
            case 0: return NotePad.Notes.WHITE_COLOR;
            case 1: return NotePad.Notes.RED_COLOR;
            case 2: return NotePad.Notes.BLUE_COLOR;
            case 3: return NotePad.Notes.YELLOW_COLOR;
            case 4: return NotePad.Notes.GREEN_COLOR;
            case 5: return NotePad.Notes.PURPLE_COLOR;
            default: return NotePad.Notes.WHITE_COLOR;
        }
    }

    private void filterNotesByColor(int color) {
        String selection = NotePad.Notes.COLUMN_NAME_BACK_COLOR + " = ?";
        String[] selectionArgs = { String.valueOf(color) };

        getLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                        NotesList.this,
                        getIntent().getData(),
                        PROJECTION,
                        selection,
                        selectionArgs,
                        NotePad.Notes.DEFAULT_SORT_ORDER
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                // 在这里，我们使用新的查询结果来更新适配器
                if (cursor != null && cursor.getCount() > 0) {
                    if (mSearchAdapter == null) {
                        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
                        int[] viewIDs = { android.R.id.text1, R.id.text2 };
                        mSearchAdapter = new SimpleCursorAdapter(
                                NotesList.this,                             // The Context for the ListView
                                R.layout.noteslist_item,                    // Points to the XML for a list item
                                cursor,                                     // The cursor to get items from
                                dataColumns,
                                viewIDs,
                                0
                        );
                        mSearchAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                            @Override
                            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                                int x = cursor.getInt(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
                                switch (x){
                                    case NotePad.Notes.WHITE_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_white));
                                        break;
                                    case NotePad.Notes.RED_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_red));
                                        break;
                                    case NotePad.Notes.BLUE_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_blue));
                                        break;
                                    case NotePad.Notes.YELLOW_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_yellow));
                                        break;
                                    case NotePad.Notes.GREEN_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_green));
                                        break;
                                    case NotePad.Notes.PURPLE_COLOR:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_purple));
                                        break;
                                    default:
                                        view.setBackgroundColor(getResources().getColor(R.color.background_white));
                                        break;
                                }
                                if (columnIndex == COLUMN_INDEX_MODIFICATION_DATE) {
                                    long timestamp = cursor.getLong(columnIndex);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00")); // 修改时区为东八区
                                    String formattedDate = dateFormat.format(new Date(timestamp));
                                    ((TextView) view).setText(formattedDate);
                                    return true;
                                }
                                return false;
                            }
                        });
                        searchResultsListView.setAdapter(mSearchAdapter); // 设置搜索结果的适配器
                    } else {
                        mSearchAdapter.swapCursor(cursor); // 使用新的游标更新适配器
                    }
                    originalListView.setVisibility(View.GONE); // 隐藏原始 ListView
                    searchResultsListView.setVisibility(View.VISIBLE);
                } else {
                    // 如果没有搜索结果，显示所有笔记并提示用户
                    Toast.makeText(NotesList.this, "没有找到对应颜色的笔记", Toast.LENGTH_SHORT).show();
                    showAllNotes();
                }
            }


            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // 当 Loader 重置时，清除适配器的数据
                if (mSearchAdapter != null) {
                    mSearchAdapter.swapCursor(null);
                }
            }
        });
    }

    // 实现导出逻辑
    private void exportNotes(Uri noteUri) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(noteUri, PROJECTION, null, null, null);

        if (cursor != null) {
            try {
                // 确保外部存储是可用的
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    File exportDir = new File(getExternalFilesDir(null), "Note export");
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }

                    // 遍历笔记并写入单独的文件
                    while (cursor.moveToNext()) {
                        String title = cursor.getString(COLUMN_INDEX_TITLE);
                        String noteText = cursor.getString(COLUMN_INDEX_NOTE);

                        // 清理标题，确保它是一个有效的文件名
                        String cleanedTitle = title.replaceAll("[^a-zA-Z0-9. ]", "_");

                        // 创建文件
                        File exportFile = new File(exportDir, cleanedTitle + ".txt");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));

                        // 写入笔记标题和内容
                        writer.write("Title: " + title + "\n\n");
                        writer.write(noteText + "\n\n");

                        writer.close();
                    }

                    Toast.makeText(this, "笔记已导出到： " + exportDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "外部存储不可用", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        }
    }
}
