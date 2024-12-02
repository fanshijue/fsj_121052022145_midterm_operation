NotePad

项目名称

NotePad记事本


介绍

移动软件开发 期中作业

121052022145 软件工程4班 范诗珏

本项目将基于https://github.com/llfjfz/NotePad提供的NotePad应用源代码实现功能扩展


作业要求

基本要求：

NoteList界面中笔记条目增加时间戳显示

添加笔记查询功能（根据标题查询）

附加功能：

UI美化、更改笔记背景颜色

导出笔记

根据笔记背景颜色进行对笔记分类


基本功能

1、增加时间戳显示

//在noteslist_item.xml中添加一个TextView，用于显示时间戳


<TextView
  
            android:id="@+id/text2"

            
            android:layout_width="match_parent"
            
            android:layout_height="match_parent"
            
            android:paddingLeft="10dp"
            
            android:paddingRight="10dp"
            
            android:layout_weight="2"
            
            android:textAppearance="?android:attr/textAppearanceSmall" />


//在NotesList类的字符串属性PROJECTION中添加一个新的变量NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE


private static final String[] PROJECTION = new String[] {

            NotePad.Notes._ID, // 0
            
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            
            NotePad.Notes.COLUMN_NAME_NOTE, //2
            
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 3 显示时间戳
            
            NotePad.Notes.COLUMN_NAME_BACK_COLOR //4 显示笔记背景颜色
    
    };


//在NotesList类中添加一个新的私有属性COLUMN_INDEX_MODIFICATION_DATE并赋值为3


private static final int COLUMN_INDEX_MODIFICATION_DATE = 3;


//修改NotesList类的onCreate方法中的相关参数


String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };

int[] viewIDs = { android.R.id.text1, R.id.text2 };


//在NotesList类的onCreate方法中添加


mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == COLUMN_INDEX_MODIFICATION_DATE) {
                    long timestamp = cursor.getLong(columnIndex);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                    String formattedDate = dateFormat.format(new Date(timestamp));
                    ((TextView) view).setText(formattedDate);
                    return true;
                }
                return false;
            }
            
        });

        
//在NotesList类的onCreateContextMenu方法中添加


Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

long modificationDate = cursor.getLong(COLUMN_INDEX_MODIFICATION_DATE);

SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());

dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

String formattedDate = dateFormat.format(new Date(modificationDate));


效果如下：

![时间戳1](https://github.com/user-attachments/assets/1f61c276-b362-4116-89b9-815edd1eb7b9)
<img width="296" alt="时间戳2" src="https://github.com/user-attachments/assets/5c5a7871-dadb-4d8c-a11d-61b827fcccba">


2、添加笔记查询功能（根据标题查询）

//在list_options_menu.xml中添加一个item：menu_search


 <item

   android:id="@+id/menu_search"
   
        android:title="@string/menu_search"
        
        android:icon="@drawable/ic_note_search"
        
        android:showAsAction="ifRoom|collapseActionView"
        
        android:actionViewClass="android.widget.SearchView" />

        
//在notes_list.xml中添加一个新的ListView：search_results_list

    
    <ListView
    
        android:id="@+id/search_results_list"
        
        android:layout_width="match_parent"
        
        android:layout_height="0dp"
        
        android:layout_weight="1"
        
        android:visibility="gone" />

        
//在NotesList类中修改并添加私有属性，用于分辨原始视图和查询结果视图


private ListView originalListView;//原始视图

private ListView searchResultsListView;//查询结果视图

private SimpleCursorAdapter mAdapter;//原始适配器

private SimpleCursorAdapter mSearchAdapter = null;//查询结果适配器


//在NotesList类的onCreate方法中添加


setContentView(R.layout.notes_list);

searchResultsListView = findViewById(R.id.search_results_list);//设置查询结果视图

originalListView = getListView();//设置原始视图

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

        
//在NotesList类的onCreateOptionsMenu方法中添加


MenuItem searchItem = menu.findItem(R.id.menu_search);

SearchView searchView = (SearchView) searchItem.getActionView();

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

        
//在NotesList类中添加方法
    
    
    private void performSearch(String query) {

        // 在开始新的搜索之前，清除适配器中的数据

        if (mSearchAdapter != null) {
        
            mSearchAdapter.swapCursor(null);
        
        }

        // 重启Loader来执行新的查询
        
        getLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                setContentView(R.layout.notes_list);

                // Initialize the new ListView for search results
                searchResultsListView = getListView();
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
    
结果如下：

<img width="192" alt="查询1" src="https://github.com/user-attachments/assets/924d2890-36b7-4604-8e45-14f44da2df59">

<img width="202" alt="查询2" src="https://github.com/user-attachments/assets/3585f5fb-e4bb-4d48-882c-37aa8c7ccd9b">


附加功能：
1、UI美化、更改笔记背景颜色（UI美化主要是修改ic图片资源，代码以更改笔记背景颜色为主）

//在NotePad中添加属性


public static final String COLUMN_NAME_BACK_COLOR = "color";

public static final int WHITE_COLOR = 0;

public static final int RED_COLOR = 1;

public static final int BLUE_COLOR = 2;

public static final int YELLOW_COLOR = 3;

public static final int GREEN_COLOR = 4;

public static final int PURPLE_COLOR = 5;


//修改NotePadProvider类的onCreate方法

@Override
public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " (" +

            NotePad.Notes._ID + " INTEGER PRIMARY KEY," +

            NotePad.Notes.COLUMN_NAME_TITLE + " TEXT," +

            NotePad.Notes.COLUMN_NAME_NOTE + " TEXT," +

            NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER," +

            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER," +

             NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER" + //数据库增加color属性

            ");");
}


//修改NotePadProvider类的实例化和设置静态对象的块，在其中添加


sNotesProjectionMap.put(

                NotePad.Notes.COLUMN_NAME_BACK_COLOR,

                NotePad.Notes.COLUMN_NAME_BACK_COLOR);


//在NotePadProvider类的insert方法中添加

if (values.containsKey(NotePad.Notes.COLUMN_NAME_BACK_COLOR) == false) {

            values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.WHITE_COLOR);

        }


//在NotesList类的字符串属性PROJECTION中添加一个新的变量NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE


private static final String[] PROJECTION = new String[] {

            NotePad.Notes._ID, // 0
            
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            
            NotePad.Notes.COLUMN_NAME_NOTE, //2
            
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 3
            
            NotePad.Notes.COLUMN_NAME_BACK_COLOR //4
    
    };


//在onCreate方法中mAdapter调用方法setViewBinder中进行覆写的方法setViewValue中添加


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


//同理，在performSearch方法mSearchAdapter调用方法setViewBinder中进行覆写的方法setViewValue中添加


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

//在values文件夹中新建colors.xml


<?xml version="1.0" encoding="utf-8"?>

<resources>

  <color name="background_red">#FFE4E1</color>
  
    <color name="background_blue">#F0F8FF</color>
    
    <color name="background_yellow">#FFFACD</color>
    
    <color name="background_green">#F0FFF0</color>
    
    <color name="background_purple">#E6E6FA</color>
    
    <color name="background_white">#FFFFFF</color>
    
    <color name="text_color">#000000</color>

</resources>


//在drawable文件夹中创建每个背景颜色对应的xml文件

  //background_blue.xml
  
  
  <?xml version="1.0" encoding="utf-8"?>
  
  
  <!-- res/drawable/color_red.xml -->
  
  <shape xmlns:android="http://schemas.android.com/apk/res/android"
  
    android:shape="rectangle">
    
    <solid android:color="#F0F8FF" />
   
    <size android:width="24dp" android:height="24dp" />
 
  </shape>
  
  
  //background_green.xml
  
  
  <?xml version="1.0" encoding="utf-8"?>

<!-- res/drawable/color_red.xml -->

<shape xmlns:android="http://schemas.android.com/apk/res/android"

  android:shape="rectangle">
  
    <solid android:color="#F0FFF0" />
    
    <size android:width="24dp" android:height="24dp" />

</shape>

  
  //background_purple.xml
  
  
  <?xml version="1.0" encoding="utf-8"?>

<!-- res/drawable/color_red.xml -->

<shape xmlns:android="http://schemas.android.com/apk/res/android"

  android:shape="rectangle">
  
    <solid android:color="#E6E6FA" />
    
    <size android:width="24dp" android:height="24dp" />

</shape>

  
  //background_red.xml

  
  <?xml version="1.0" encoding="utf-8"?>

<!-- res/drawable/color_red.xml -->

<shape xmlns:android="http://schemas.android.com/apk/res/android"

  android:shape="rectangle">
  
    <solid android:color="#FFE4E1" />
    
    <size android:width="24dp" android:height="24dp" />

</shape>

  
  //background_white.xml
  
  
  <?xml version="1.0" encoding="utf-8"?>

<!-- res/drawable/color_red.xml -->

<shape xmlns:android="http://schemas.android.com/apk/res/android"

  android:shape="rectangle">
  
    <solid android:color="#FFFFFF" />
    
    <size android:width="24dp" android:height="24dp" />

</shape>

  
  //background_yellow.xml
  
  <?xml version="1.0" encoding="utf-8"?>

<!-- res/drawable/color_red.xml -->

<shape xmlns:android="http://schemas.android.com/apk/res/android"

  android:shape="rectangle">
  
    <solid android:color="#FFFACD" />
    
    <size android:width="24dp" android:height="24dp" />

</shape>



//在editor_options_menu.xml中添加一个item

 
 <item
 
   android:id="@+id/change_background"
   
        android:title="修改笔记背景颜色">
        
        <menu>
        
            <group>
            
                <item android:id="@+id/red"
                
                    android:title="红色"
                    
                    android:icon="@drawable/background_red"/>
                
                <item android:id="@+id/blue"
                
                    android:title="蓝色"
                    
                    android:icon="@drawable/background_blue"/>
                
                <item android:id="@+id/yellow"
                
                    android:title="黄色"
                    
                    android:icon="@drawable/background_yellow"/>
                
                <item android:id="@+id/green"
                
                    android:title="绿色"
                    
                    android:icon="@drawable/background_green"/>
                
                <item android:id="@+id/purple"
                
                    android:title="紫色"
                    
                    android:icon="@drawable/background_purple"/>
                
                <item android:id="@+id/white"
                
                    android:title="白色"
                    
                    android:icon="@drawable/background_white"/>
            
            </group>
        
        </menu>
    
    </item>


//在NoteEditor类的字符串属性PROJECTION中添加一个新的变量NotePad.Notes.COLUMN_NAME_BACK_COLOR


private static final String[] PROJECTION =

        new String[] {
        
            NotePad.Notes._ID,
            
            NotePad.Notes.COLUMN_NAME_TITLE,
            
            NotePad.Notes.COLUMN_NAME_NOTE,
            
                NotePad.Notes.COLUMN_NAME_BACK_COLOR
    
    };


//在NoteEditor类的onResume方法中添加


if (mCursor != null) {
                
                mCursor.moveToFirst();
                
                int x = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);

                int y = mCursor.getInt(x);

                Log.i("NoteEditor", "color"+y);

            switch (y){
                
                case NotePad.Notes.WHITE_COLOR:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_white));
                    
                    break;
                
                case NotePad.Notes.RED_COLOR:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_red));
                    
                    break;
                
                case NotePad.Notes.BLUE_COLOR:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_blue));
                    
                    break;
                
                case NotePad.Notes.YELLOW_COLOR:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_yellow));
                    
                    break;
                
                case NotePad.Notes.GREEN_COLOR:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_green));
                    
                    break;
                
                case NotePad.Notes.PURPLE_COLOR:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_purple));
                    
                    break;
                
                default:
                
                    mText.setBackgroundColor(getResources().getColor(R.color.background_white));
                    
                    break;
            
            }


//在NoteEditor类的onOptionsItemSelected方法在添加


noteview = findViewById(R.id.note);

ContentValues values = new ContentValues();


//在NoteEditor类的onOptionsItemSelected方法的switch中添加


case R.id.red:

        noteview.setBackgroundColor(getResources().getColor(R.color.background_red));
        
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.RED_COLOR);
        
        getContentResolver().update(mUri, values, null, null);
        
        break;

case R.id.blue:
    
        noteview.setBackgroundColor(getResources().getColor(R.color.background_blue));
        
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.BLUE_COLOR);
        
        getContentResolver().update(mUri, values, null, null);
        
        break;

case R.id.yellow:

        noteview.setBackgroundColor(getResources().getColor(R.color.background_yellow));
        
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.YELLOW_COLOR);
        
        getContentResolver().update(mUri, values, null, null);
        
        break;

case R.id.green:

        noteview.setBackgroundColor(getResources().getColor(R.color.background_green));
        
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.GREEN_COLOR);
        
        getContentResolver().update(mUri, values, null, null);
        
        break;

case R.id.purple:

        noteview.setBackgroundColor(getResources().getColor(R.color.background_purple));
        
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.PURPLE_COLOR);
        
        getContentResolver().update(mUri, values, null, null);
        
        break;

case R.id.white:

        noteview.setBackgroundColor(getResources().getColor(R.color.background_white));
        
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.WHITE_COLOR);
        
        getContentResolver().update(mUri, values, null, null);

效果如下：

<img width="197" alt="修改背景颜色1" src="https://github.com/user-attachments/assets/4f220ccb-2566-4d4c-bd69-ab4b2b0f8761">

<img width="192" alt="修改背景颜色2" src="https://github.com/user-attachments/assets/fa84fdc9-c6d0-4fef-bbd1-f62887cb88e3">




2、导出笔记

//在list_context_menu.xml中添加一个item


<item android:id="@+id/context_export"
        
  android:title="@string/menu_export" />


//在NotesList的onContextItemSelected方法的switch中添加、

 case R.id.context_export:
 
            exportNotes(noteUri);
            
            return true;


//在NotesList类中添加方法


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

                    while (cursor.moveToNext()) {
                        
                        String title = cursor.getString(COLUMN_INDEX_TITLE);
                        
                        String noteText = cursor.getString(COLUMN_INDEX_NOTE);

                        String cleanedTitle = title.replaceAll("[^a-zA-Z0-9. ]", "_");

                        File exportFile = new File(exportDir, cleanedTitle + ".txt");
                        
                        BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));

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

效果如下：

<img width="212" alt="导出笔记1" src="https://github.com/user-attachments/assets/e4f9e318-92c1-4535-98c0-5b12f2cba259">

<img width="197" alt="导出笔记2" src="https://github.com/user-attachments/assets/f5a8b0d7-86dc-4796-bf40-ed260d1512fd">

3、根据笔记背景颜色进行对笔记分类

//在layout文件夹中新建一个context_color_item.xml


<?xml version="1.0" encoding="utf-8"?>

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"

  android:layout_width="match_parent"
  
    android:layout_height="wrap_content"
    
    android:orientation="horizontal"
    
    android:padding="8dp">

<ImageView

  android:id="@+id/color_icon"
  
        android:layout_width="wrap_content"
        
        android:layout_height="wrap_content"
        
        android:layout_marginRight="10dp" />

<TextView

  android:id="@+id/color_text"
  
        android:layout_width="wrap_content"
        
        android:layout_height="wrap_content"
        
        android:textSize="18sp" />

</LinearLayout>


//在list_options_menu.xml中添加一个item


<item

  android:id="@+id/menu_filter_by_color"
  
        android:title="@string/menu_filter_by_color"
        
        android:showAsAction="ifRoom"/>


//在NotesList的onOptionsItemSelected方法的switch中添加


case R.id.menu_filter_by_color:

            showColorFilterDialog();

            return true;


//在NotesList中添加


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

效果如下：

<img width="194" alt="分类1" src="https://github.com/user-attachments/assets/94b6489b-45f4-412a-a9e6-41454dd91ac4">
<img width="199" alt="分类2" src="https://github.com/user-attachments/assets/d126855a-5fb7-43fe-b5a4-bc47832b5b0e">

