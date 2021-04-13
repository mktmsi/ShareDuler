<?php

class DBHelper{
  private $link;
  private $db_selected;

  public function __construct(){
    $this->link = mysql_connect('localhost', 'shareduler_user', 'jphacks2o17');
    if (!$this->link){
      die('接続失敗です．'.mysql_error());
    }

    $this->db_selected = mysql_select_db('shareduler', $this->link);
    if (!$this->db_selected){
        die('データベース選択失敗です。'.mysql_error());
    }
  }

  public function __destruct(){
    mysql_close($this->link);
  }

  public function insert_event($schedule, $dateMillis, $placeName, $latitude, $longitude, $url, $name){
    $sql = "INSERT INTO events (schedule, dateMillis, placeName, latitude, longitude, url, creatorName) VALUES ('";
    $sql .= $schedule;
    $sql .= "',";
    $sql .= $dateMillis;
    $sql .= ",'";
    //$sql .= strval($placeName);
    $sql .= htmlspecialchars($placeName, ENT_QUOTES, "utf-8");
    $sql .= "',";
    $sql .= $latitude;
    $sql .= ",";
    $sql .= $longitude;
    $sql .= ",'";
    $sql .= $url;
    $sql .= "',";
    $sql .= $name;
    $sql .= ")";
    $result_flag = mysql_query($sql);

    if (!$result_flag) {
          die('INSERTクエリーが失敗しました。'.mysql_error());
    }
    $last_id = mysql_insert_id();
    $this->create_table($last_id);
  }

  public function reset_event(){
    $sql = "TRUNCATE TABLE events"; 
    $result_flag = mysql_query($sql);

    if (!$result_flag) {
          die('TRUNCATEクエリーが失敗しました。'.mysql_error());
    }
  }

  public function get_event($url){
    $sql = "SELECT * FROM events WHERE url = '".$url."'";
    $result = mysql_query($sql);
    if (!$result) {
          die('SELECTクエリーが失敗しました。'.mysql_error());
    }
    $row = mysql_fetch_assoc($result);
    return $row;
  }

  public function get_eventName($url){
    $sql = "SELECT id FROM events WHERE url = '".$url."'";
    $result = mysql_query($sql);
    if(!$result){
      die('SELECTクエリーが失敗しました。'.mysql_error());
    }
    $row = mysql_fetch_assoc($result);
    $event_table = "event".$row['id'];
    return $event_table;
  }

  public function get_user($id){
    $sql = "SELECT * FROM user WHERE id = ".$id;
    $result = mysql_query($sql);
    if (!$result) {
          die('SELECTクエリーが失敗しました。'.mysql_error());
    }
    $row = mysql_fetch_assoc($result);
    return $row;
    
  }

  public function insert_user($name){
    $sql = "INSERT INTO user (name) VALUES ('";
    $sql .= $name;
    $sql .= "')";
    $result_flag = mysql_query($sql);
    $last_id = mysql_insert_id();

    if (!$result_flag) {
          die('INSERTクエリーが失敗しました。'.mysql_error());
    }
    return $last_id;
  }

  public function update_user($id, $name){
    $sql = "UPDATE user SET name = '".$name."' WHERE id = ".$id.";";
    $result_flag = mysql_query($sql);

    if (!$result_flag) {
          die('UPDATEクエリーが失敗しました。'.mysql_error());
    }
  }

  public function reset_user(){
    $sql = "TRUNCATE TABLE user"; 
    $result_flag = mysql_query($sql);

    if (!$result_flag) {
          die('TRUNCATEクエリーが失敗しました。'.mysql_error());
    }
  }

  public function create_table($id){
    $table_name = "event".$id;
    $sql = "CREATE TABLE `{$table_name}` "
      ."("
      ."id INT NOT NULL, "
      ."mode INT NOT NULL, "
      ."distance INT NOT NULL, "
      ."timeMillis INT NOT NULL"
      .");";
    $result_flag = mysql_query($sql);
    if (!$result_flag) {
          die('CREATEクエリーが失敗しました。'.mysql_error());
    }
    $sql = "ALTER TABLE `{$table_name}` ADD PRIMARY KEY (id);";
    $result_flag = mysql_query($sql);
    if (!$result_flag) {
          die('ALTERクエリーが失敗しました。'.mysql_error());
    }
  }

  public function remove_table(){
  }

}

?>
