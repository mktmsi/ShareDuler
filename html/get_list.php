<?php
ini_set('display_errors',1);

include 'DBHelper.php';

$db = new DBHelper();

$url = $_GET['url'];

$eventName = $db->get_eventName($url);
$sql = "SELECT * FROM `{$eventName}`";
$result = mysql_query($sql);
if (!$result) {
  die('selectクエリーが失敗しました'.mysql_error());
}
/*
while ($row = mysql_fetch_assoc($result)){
  echo " id:".$row["id"];
  echo " mode:".$row["mode"];
  echo " distance:".$row["distance"];
  echo " timeMillis:".$row["timeMillis"];
  echo nl2br("\n");
}
 */

$json = array();
$i = 0;
while ($row = mysql_fetch_assoc($result)){
  $name_row = $db->get_user($row['id']);
  $name = $name_row['name'];
  $jsonArray = array(
    "name" => $name,
    "mode" => $row["mode"],
    "distance" => $row["distance"],
    "timeMillis" => $row["timeMillis"]
  );
  $json += $json + array($i => $jsonArray);
  $i+=1;
}

header("Content-Type: application/json; charset=UTF-8");
echo json_encode($json, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE);

?>

