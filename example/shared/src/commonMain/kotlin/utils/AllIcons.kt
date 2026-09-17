// Copyright 2025, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package utils

import androidx.compose.ui.graphics.vector.ImageVector
import site.unclefish.yubeix.icon.YubeixIcons
import site.unclefish.yubeix.icon.extended.Add
import site.unclefish.yubeix.icon.extended.AddCircle
import site.unclefish.yubeix.icon.extended.AddFolder
import site.unclefish.yubeix.icon.extended.Alarm
import site.unclefish.yubeix.icon.extended.Album
import site.unclefish.yubeix.icon.extended.All
import site.unclefish.yubeix.icon.extended.Answer
import site.unclefish.yubeix.icon.extended.AppRecording
import site.unclefish.yubeix.icon.extended.Back
import site.unclefish.yubeix.icon.extended.Background
import site.unclefish.yubeix.icon.extended.Backup
import site.unclefish.yubeix.icon.extended.BankCards
import site.unclefish.yubeix.icon.extended.Blocklist
import site.unclefish.yubeix.icon.extended.CallRecording
import site.unclefish.yubeix.icon.extended.Carrier
import site.unclefish.yubeix.icon.extended.ChevronBackward
import site.unclefish.yubeix.icon.extended.ChevronForward
import site.unclefish.yubeix.icon.extended.Clear
import site.unclefish.yubeix.icon.extended.Close
import site.unclefish.yubeix.icon.extended.Close2
import site.unclefish.yubeix.icon.extended.CloudFill
import site.unclefish.yubeix.icon.extended.Community
import site.unclefish.yubeix.icon.extended.Contacts
import site.unclefish.yubeix.icon.extended.ContactsBook
import site.unclefish.yubeix.icon.extended.ContactsCircle
import site.unclefish.yubeix.icon.extended.ConvertFile
import site.unclefish.yubeix.icon.extended.Copy
import site.unclefish.yubeix.icon.extended.Create
import site.unclefish.yubeix.icon.extended.Cut
import site.unclefish.yubeix.icon.extended.Delete
import site.unclefish.yubeix.icon.extended.Download
import site.unclefish.yubeix.icon.extended.Edit
import site.unclefish.yubeix.icon.extended.Email
import site.unclefish.yubeix.icon.extended.ExpandLess
import site.unclefish.yubeix.icon.extended.ExpandMore
import site.unclefish.yubeix.icon.extended.Favorites
import site.unclefish.yubeix.icon.extended.FavoritesFill
import site.unclefish.yubeix.icon.extended.File
import site.unclefish.yubeix.icon.extended.FileDownloads
import site.unclefish.yubeix.icon.extended.Filter
import site.unclefish.yubeix.icon.extended.Folder
import site.unclefish.yubeix.icon.extended.FolderFill
import site.unclefish.yubeix.icon.extended.Forward
import site.unclefish.yubeix.icon.extended.GridView
import site.unclefish.yubeix.icon.extended.Help
import site.unclefish.yubeix.icon.extended.Hide
import site.unclefish.yubeix.icon.extended.HorizontalSplit
import site.unclefish.yubeix.icon.extended.Image
import site.unclefish.yubeix.icon.extended.Import
import site.unclefish.yubeix.icon.extended.Info
import site.unclefish.yubeix.icon.extended.Layers
import site.unclefish.yubeix.icon.extended.Link
import site.unclefish.yubeix.icon.extended.ListView
import site.unclefish.yubeix.icon.extended.Location
import site.unclefish.yubeix.icon.extended.Lock
import site.unclefish.yubeix.icon.extended.MapAlbum
import site.unclefish.yubeix.icon.extended.Merge
import site.unclefish.yubeix.icon.extended.Messages
import site.unclefish.yubeix.icon.extended.Mic
import site.unclefish.yubeix.icon.extended.MicSlash
import site.unclefish.yubeix.icon.extended.MindMap
import site.unclefish.yubeix.icon.extended.Months
import site.unclefish.yubeix.icon.extended.More
import site.unclefish.yubeix.icon.extended.MoreCircle
import site.unclefish.yubeix.icon.extended.MoveFile
import site.unclefish.yubeix.icon.extended.Music
import site.unclefish.yubeix.icon.extended.Notes
import site.unclefish.yubeix.icon.extended.NotesFill
import site.unclefish.yubeix.icon.extended.Ok
import site.unclefish.yubeix.icon.extended.Paste
import site.unclefish.yubeix.icon.extended.Pause
import site.unclefish.yubeix.icon.extended.Phone
import site.unclefish.yubeix.icon.extended.Photos
import site.unclefish.yubeix.icon.extended.Pin
import site.unclefish.yubeix.icon.extended.Play
import site.unclefish.yubeix.icon.extended.Playlist
import site.unclefish.yubeix.icon.extended.Promotions
import site.unclefish.yubeix.icon.extended.Recent
import site.unclefish.yubeix.icon.extended.Recording
import site.unclefish.yubeix.icon.extended.RecordingTape
import site.unclefish.yubeix.icon.extended.Redo
import site.unclefish.yubeix.icon.extended.Refresh
import site.unclefish.yubeix.icon.extended.Remove
import site.unclefish.yubeix.icon.extended.RemoveContact
import site.unclefish.yubeix.icon.extended.Rename
import site.unclefish.yubeix.icon.extended.Replace
import site.unclefish.yubeix.icon.extended.Reply
import site.unclefish.yubeix.icon.extended.ReplyAll
import site.unclefish.yubeix.icon.extended.Report
import site.unclefish.yubeix.icon.extended.Reset
import site.unclefish.yubeix.icon.extended.RotateLeft
import site.unclefish.yubeix.icon.extended.Scan
import site.unclefish.yubeix.icon.extended.ScreenCapture
import site.unclefish.yubeix.icon.extended.ScreenMirroring
import site.unclefish.yubeix.icon.extended.Search
import site.unclefish.yubeix.icon.extended.SearchDevice
import site.unclefish.yubeix.icon.extended.SelectAll
import site.unclefish.yubeix.icon.extended.Send
import site.unclefish.yubeix.icon.extended.Settings
import site.unclefish.yubeix.icon.extended.Share
import site.unclefish.yubeix.icon.extended.Show
import site.unclefish.yubeix.icon.extended.Sidebar
import site.unclefish.yubeix.icon.extended.Sort
import site.unclefish.yubeix.icon.extended.Stopwatch
import site.unclefish.yubeix.icon.extended.Store
import site.unclefish.yubeix.icon.extended.Tasks
import site.unclefish.yubeix.icon.extended.Th1
import site.unclefish.yubeix.icon.extended.Th10
import site.unclefish.yubeix.icon.extended.Th11
import site.unclefish.yubeix.icon.extended.Th12
import site.unclefish.yubeix.icon.extended.Th13
import site.unclefish.yubeix.icon.extended.Th14
import site.unclefish.yubeix.icon.extended.Th15
import site.unclefish.yubeix.icon.extended.Th16
import site.unclefish.yubeix.icon.extended.Th17
import site.unclefish.yubeix.icon.extended.Th18
import site.unclefish.yubeix.icon.extended.Th19
import site.unclefish.yubeix.icon.extended.Th2
import site.unclefish.yubeix.icon.extended.Th20
import site.unclefish.yubeix.icon.extended.Th21
import site.unclefish.yubeix.icon.extended.Th22
import site.unclefish.yubeix.icon.extended.Th23
import site.unclefish.yubeix.icon.extended.Th24
import site.unclefish.yubeix.icon.extended.Th25
import site.unclefish.yubeix.icon.extended.Th26
import site.unclefish.yubeix.icon.extended.Th27
import site.unclefish.yubeix.icon.extended.Th28
import site.unclefish.yubeix.icon.extended.Th29
import site.unclefish.yubeix.icon.extended.Th3
import site.unclefish.yubeix.icon.extended.Th30
import site.unclefish.yubeix.icon.extended.Th31
import site.unclefish.yubeix.icon.extended.Th4
import site.unclefish.yubeix.icon.extended.Th5
import site.unclefish.yubeix.icon.extended.Th6
import site.unclefish.yubeix.icon.extended.Th7
import site.unclefish.yubeix.icon.extended.Th8
import site.unclefish.yubeix.icon.extended.Th9
import site.unclefish.yubeix.icon.extended.Theme
import site.unclefish.yubeix.icon.extended.Timer
import site.unclefish.yubeix.icon.extended.TopDownloads
import site.unclefish.yubeix.icon.extended.Translate
import site.unclefish.yubeix.icon.extended.Trim
import site.unclefish.yubeix.icon.extended.Tune
import site.unclefish.yubeix.icon.extended.Undo
import site.unclefish.yubeix.icon.extended.Unlock
import site.unclefish.yubeix.icon.extended.Unpin
import site.unclefish.yubeix.icon.extended.Update
import site.unclefish.yubeix.icon.extended.UploadCloud
import site.unclefish.yubeix.icon.extended.VerticalSplit
import site.unclefish.yubeix.icon.extended.VolumeOff
import site.unclefish.yubeix.icon.extended.VolumeUp
import site.unclefish.yubeix.icon.extended.Weeks
import site.unclefish.yubeix.icon.extended.WorldClock
import site.unclefish.yubeix.icon.extended.Years
import site.unclefish.yubeix.icon.extended.ZoomOut

val YubeixIcons.All: Map<String, List<ImageVector>>
    get() = mapOf(
        "Light" to listOf(
            YubeixIcons.Light.Add,
            YubeixIcons.Light.AddCircle,
            YubeixIcons.Light.AddFolder,
            YubeixIcons.Light.Alarm,
            YubeixIcons.Light.Album,
            YubeixIcons.Light.All,
            YubeixIcons.Light.Answer,
            YubeixIcons.Light.AppRecording,
            YubeixIcons.Light.Back,
            YubeixIcons.Light.Background,
            YubeixIcons.Light.Backup,
            YubeixIcons.Light.BankCards,
            YubeixIcons.Light.Blocklist,
            YubeixIcons.Light.CallRecording,
            YubeixIcons.Light.Carrier,
            YubeixIcons.Light.ChevronBackward,
            YubeixIcons.Light.ChevronForward,
            YubeixIcons.Light.Clear,
            YubeixIcons.Light.Close,
            YubeixIcons.Light.Close2,
            YubeixIcons.Light.CloudFill,
            YubeixIcons.Light.Community,
            YubeixIcons.Light.Contacts,
            YubeixIcons.Light.ContactsBook,
            YubeixIcons.Light.ContactsCircle,
            YubeixIcons.Light.ConvertFile,
            YubeixIcons.Light.Copy,
            YubeixIcons.Light.Create,
            YubeixIcons.Light.Cut,
            YubeixIcons.Light.Delete,
            YubeixIcons.Light.Download,
            YubeixIcons.Light.Edit,
            YubeixIcons.Light.Email,
            YubeixIcons.Light.ExpandLess,
            YubeixIcons.Light.ExpandMore,
            YubeixIcons.Light.Favorites,
            YubeixIcons.Light.FavoritesFill,
            YubeixIcons.Light.File,
            YubeixIcons.Light.FileDownloads,
            YubeixIcons.Light.Filter,
            YubeixIcons.Light.Folder,
            YubeixIcons.Light.FolderFill,
            YubeixIcons.Light.Forward,
            YubeixIcons.Light.GridView,
            YubeixIcons.Light.Help,
            YubeixIcons.Light.Hide,
            YubeixIcons.Light.HorizontalSplit,
            YubeixIcons.Light.Image,
            YubeixIcons.Light.Import,
            YubeixIcons.Light.Info,
            YubeixIcons.Light.Layers,
            YubeixIcons.Light.Link,
            YubeixIcons.Light.ListView,
            YubeixIcons.Light.Location,
            YubeixIcons.Light.Lock,
            YubeixIcons.Light.MapAlbum,
            YubeixIcons.Light.Merge,
            YubeixIcons.Light.Messages,
            YubeixIcons.Light.Mic,
            YubeixIcons.Light.MicSlash,
            YubeixIcons.Light.MindMap,
            YubeixIcons.Light.Months,
            YubeixIcons.Light.More,
            YubeixIcons.Light.MoreCircle,
            YubeixIcons.Light.MoveFile,
            YubeixIcons.Light.Music,
            YubeixIcons.Light.Notes,
            YubeixIcons.Light.NotesFill,
            YubeixIcons.Light.Ok,
            YubeixIcons.Light.Paste,
            YubeixIcons.Light.Pause,
            YubeixIcons.Light.Phone,
            YubeixIcons.Light.Photos,
            YubeixIcons.Light.Pin,
            YubeixIcons.Light.Play,
            YubeixIcons.Light.Playlist,
            YubeixIcons.Light.Promotions,
            YubeixIcons.Light.Recent,
            YubeixIcons.Light.Recording,
            YubeixIcons.Light.RecordingTape,
            YubeixIcons.Light.Redo,
            YubeixIcons.Light.Refresh,
            YubeixIcons.Light.Remove,
            YubeixIcons.Light.RemoveContact,
            YubeixIcons.Light.Rename,
            YubeixIcons.Light.Replace,
            YubeixIcons.Light.Reply,
            YubeixIcons.Light.ReplyAll,
            YubeixIcons.Light.Report,
            YubeixIcons.Light.Reset,
            YubeixIcons.Light.RotateLeft,
            YubeixIcons.Light.Scan,
            YubeixIcons.Light.ScreenCapture,
            YubeixIcons.Light.ScreenMirroring,
            YubeixIcons.Light.Search,
            YubeixIcons.Light.SearchDevice,
            YubeixIcons.Light.SelectAll,
            YubeixIcons.Light.Send,
            YubeixIcons.Light.Settings,
            YubeixIcons.Light.Share,
            YubeixIcons.Light.Show,
            YubeixIcons.Light.Sidebar,
            YubeixIcons.Light.Sort,
            YubeixIcons.Light.Stopwatch,
            YubeixIcons.Light.Store,
            YubeixIcons.Light.Tasks,
            YubeixIcons.Light.Th1,
            YubeixIcons.Light.Th10,
            YubeixIcons.Light.Th11,
            YubeixIcons.Light.Th12,
            YubeixIcons.Light.Th13,
            YubeixIcons.Light.Th14,
            YubeixIcons.Light.Th15,
            YubeixIcons.Light.Th16,
            YubeixIcons.Light.Th17,
            YubeixIcons.Light.Th18,
            YubeixIcons.Light.Th19,
            YubeixIcons.Light.Th2,
            YubeixIcons.Light.Th20,
            YubeixIcons.Light.Th21,
            YubeixIcons.Light.Th22,
            YubeixIcons.Light.Th23,
            YubeixIcons.Light.Th24,
            YubeixIcons.Light.Th25,
            YubeixIcons.Light.Th26,
            YubeixIcons.Light.Th27,
            YubeixIcons.Light.Th28,
            YubeixIcons.Light.Th29,
            YubeixIcons.Light.Th3,
            YubeixIcons.Light.Th30,
            YubeixIcons.Light.Th31,
            YubeixIcons.Light.Th4,
            YubeixIcons.Light.Th5,
            YubeixIcons.Light.Th6,
            YubeixIcons.Light.Th7,
            YubeixIcons.Light.Th8,
            YubeixIcons.Light.Th9,
            YubeixIcons.Light.Theme,
            YubeixIcons.Light.Timer,
            YubeixIcons.Light.TopDownloads,
            YubeixIcons.Light.Translate,
            YubeixIcons.Light.Trim,
            YubeixIcons.Light.Tune,
            YubeixIcons.Light.Undo,
            YubeixIcons.Light.Unlock,
            YubeixIcons.Light.Unpin,
            YubeixIcons.Light.Update,
            YubeixIcons.Light.UploadCloud,
            YubeixIcons.Light.VerticalSplit,
            YubeixIcons.Light.VolumeOff,
            YubeixIcons.Light.VolumeUp,
            YubeixIcons.Light.Weeks,
            YubeixIcons.Light.WorldClock,
            YubeixIcons.Light.Years,
            YubeixIcons.Light.ZoomOut,
        ),
        "Regular" to listOf(
            YubeixIcons.Regular.Add,
            YubeixIcons.Regular.AddCircle,
            YubeixIcons.Regular.AddFolder,
            YubeixIcons.Regular.Alarm,
            YubeixIcons.Regular.Album,
            YubeixIcons.Regular.All,
            YubeixIcons.Regular.Answer,
            YubeixIcons.Regular.AppRecording,
            YubeixIcons.Regular.Back,
            YubeixIcons.Regular.Background,
            YubeixIcons.Regular.Backup,
            YubeixIcons.Regular.BankCards,
            YubeixIcons.Regular.Blocklist,
            YubeixIcons.Regular.CallRecording,
            YubeixIcons.Regular.Carrier,
            YubeixIcons.Regular.ChevronBackward,
            YubeixIcons.Regular.ChevronForward,
            YubeixIcons.Regular.Clear,
            YubeixIcons.Regular.Close,
            YubeixIcons.Regular.Close2,
            YubeixIcons.Regular.CloudFill,
            YubeixIcons.Regular.Community,
            YubeixIcons.Regular.Contacts,
            YubeixIcons.Regular.ContactsBook,
            YubeixIcons.Regular.ContactsCircle,
            YubeixIcons.Regular.ConvertFile,
            YubeixIcons.Regular.Copy,
            YubeixIcons.Regular.Create,
            YubeixIcons.Regular.Cut,
            YubeixIcons.Regular.Delete,
            YubeixIcons.Regular.Download,
            YubeixIcons.Regular.Edit,
            YubeixIcons.Regular.Email,
            YubeixIcons.Regular.ExpandLess,
            YubeixIcons.Regular.ExpandMore,
            YubeixIcons.Regular.Favorites,
            YubeixIcons.Regular.FavoritesFill,
            YubeixIcons.Regular.File,
            YubeixIcons.Regular.FileDownloads,
            YubeixIcons.Regular.Filter,
            YubeixIcons.Regular.Folder,
            YubeixIcons.Regular.FolderFill,
            YubeixIcons.Regular.Forward,
            YubeixIcons.Regular.GridView,
            YubeixIcons.Regular.Help,
            YubeixIcons.Regular.Hide,
            YubeixIcons.Regular.HorizontalSplit,
            YubeixIcons.Regular.Image,
            YubeixIcons.Regular.Import,
            YubeixIcons.Regular.Info,
            YubeixIcons.Regular.Layers,
            YubeixIcons.Regular.Link,
            YubeixIcons.Regular.ListView,
            YubeixIcons.Regular.Location,
            YubeixIcons.Regular.Lock,
            YubeixIcons.Regular.MapAlbum,
            YubeixIcons.Regular.Merge,
            YubeixIcons.Regular.Messages,
            YubeixIcons.Regular.Mic,
            YubeixIcons.Regular.MicSlash,
            YubeixIcons.Regular.MindMap,
            YubeixIcons.Regular.Months,
            YubeixIcons.Regular.More,
            YubeixIcons.Regular.MoreCircle,
            YubeixIcons.Regular.MoveFile,
            YubeixIcons.Regular.Music,
            YubeixIcons.Regular.Notes,
            YubeixIcons.Regular.NotesFill,
            YubeixIcons.Regular.Ok,
            YubeixIcons.Regular.Paste,
            YubeixIcons.Regular.Pause,
            YubeixIcons.Regular.Phone,
            YubeixIcons.Regular.Photos,
            YubeixIcons.Regular.Pin,
            YubeixIcons.Regular.Play,
            YubeixIcons.Regular.Playlist,
            YubeixIcons.Regular.Promotions,
            YubeixIcons.Regular.Recent,
            YubeixIcons.Regular.Recording,
            YubeixIcons.Regular.RecordingTape,
            YubeixIcons.Regular.Redo,
            YubeixIcons.Regular.Refresh,
            YubeixIcons.Regular.Remove,
            YubeixIcons.Regular.RemoveContact,
            YubeixIcons.Regular.Rename,
            YubeixIcons.Regular.Replace,
            YubeixIcons.Regular.Reply,
            YubeixIcons.Regular.ReplyAll,
            YubeixIcons.Regular.Report,
            YubeixIcons.Regular.Reset,
            YubeixIcons.Regular.RotateLeft,
            YubeixIcons.Regular.Scan,
            YubeixIcons.Regular.ScreenCapture,
            YubeixIcons.Regular.ScreenMirroring,
            YubeixIcons.Regular.Search,
            YubeixIcons.Regular.SearchDevice,
            YubeixIcons.Regular.SelectAll,
            YubeixIcons.Regular.Send,
            YubeixIcons.Regular.Settings,
            YubeixIcons.Regular.Share,
            YubeixIcons.Regular.Show,
            YubeixIcons.Regular.Sidebar,
            YubeixIcons.Regular.Sort,
            YubeixIcons.Regular.Stopwatch,
            YubeixIcons.Regular.Store,
            YubeixIcons.Regular.Tasks,
            YubeixIcons.Regular.Th1,
            YubeixIcons.Regular.Th10,
            YubeixIcons.Regular.Th11,
            YubeixIcons.Regular.Th12,
            YubeixIcons.Regular.Th13,
            YubeixIcons.Regular.Th14,
            YubeixIcons.Regular.Th15,
            YubeixIcons.Regular.Th16,
            YubeixIcons.Regular.Th17,
            YubeixIcons.Regular.Th18,
            YubeixIcons.Regular.Th19,
            YubeixIcons.Regular.Th2,
            YubeixIcons.Regular.Th20,
            YubeixIcons.Regular.Th21,
            YubeixIcons.Regular.Th22,
            YubeixIcons.Regular.Th23,
            YubeixIcons.Regular.Th24,
            YubeixIcons.Regular.Th25,
            YubeixIcons.Regular.Th26,
            YubeixIcons.Regular.Th27,
            YubeixIcons.Regular.Th28,
            YubeixIcons.Regular.Th29,
            YubeixIcons.Regular.Th3,
            YubeixIcons.Regular.Th30,
            YubeixIcons.Regular.Th31,
            YubeixIcons.Regular.Th4,
            YubeixIcons.Regular.Th5,
            YubeixIcons.Regular.Th6,
            YubeixIcons.Regular.Th7,
            YubeixIcons.Regular.Th8,
            YubeixIcons.Regular.Th9,
            YubeixIcons.Regular.Theme,
            YubeixIcons.Regular.Timer,
            YubeixIcons.Regular.TopDownloads,
            YubeixIcons.Regular.Translate,
            YubeixIcons.Regular.Trim,
            YubeixIcons.Regular.Tune,
            YubeixIcons.Regular.Undo,
            YubeixIcons.Regular.Unlock,
            YubeixIcons.Regular.Unpin,
            YubeixIcons.Regular.Update,
            YubeixIcons.Regular.UploadCloud,
            YubeixIcons.Regular.VerticalSplit,
            YubeixIcons.Regular.VolumeOff,
            YubeixIcons.Regular.VolumeUp,
            YubeixIcons.Regular.Weeks,
            YubeixIcons.Regular.WorldClock,
            YubeixIcons.Regular.Years,
            YubeixIcons.Regular.ZoomOut,
        ),
        "Heavy" to listOf(
            YubeixIcons.Heavy.Add,
            YubeixIcons.Heavy.AddCircle,
            YubeixIcons.Heavy.AddFolder,
            YubeixIcons.Heavy.Alarm,
            YubeixIcons.Heavy.Album,
            YubeixIcons.Heavy.All,
            YubeixIcons.Heavy.Answer,
            YubeixIcons.Heavy.AppRecording,
            YubeixIcons.Heavy.Back,
            YubeixIcons.Heavy.Background,
            YubeixIcons.Heavy.Backup,
            YubeixIcons.Heavy.BankCards,
            YubeixIcons.Heavy.Blocklist,
            YubeixIcons.Heavy.CallRecording,
            YubeixIcons.Heavy.Carrier,
            YubeixIcons.Heavy.ChevronBackward,
            YubeixIcons.Heavy.ChevronForward,
            YubeixIcons.Heavy.Clear,
            YubeixIcons.Heavy.Close,
            YubeixIcons.Heavy.Close2,
            YubeixIcons.Heavy.CloudFill,
            YubeixIcons.Heavy.Community,
            YubeixIcons.Heavy.Contacts,
            YubeixIcons.Heavy.ContactsBook,
            YubeixIcons.Heavy.ContactsCircle,
            YubeixIcons.Heavy.ConvertFile,
            YubeixIcons.Heavy.Copy,
            YubeixIcons.Heavy.Create,
            YubeixIcons.Heavy.Cut,
            YubeixIcons.Heavy.Delete,
            YubeixIcons.Heavy.Download,
            YubeixIcons.Heavy.Edit,
            YubeixIcons.Heavy.Email,
            YubeixIcons.Heavy.ExpandLess,
            YubeixIcons.Heavy.ExpandMore,
            YubeixIcons.Heavy.Favorites,
            YubeixIcons.Heavy.FavoritesFill,
            YubeixIcons.Heavy.File,
            YubeixIcons.Heavy.FileDownloads,
            YubeixIcons.Heavy.Filter,
            YubeixIcons.Heavy.Folder,
            YubeixIcons.Heavy.FolderFill,
            YubeixIcons.Heavy.Forward,
            YubeixIcons.Heavy.GridView,
            YubeixIcons.Heavy.Help,
            YubeixIcons.Heavy.Hide,
            YubeixIcons.Heavy.HorizontalSplit,
            YubeixIcons.Heavy.Image,
            YubeixIcons.Heavy.Import,
            YubeixIcons.Heavy.Info,
            YubeixIcons.Heavy.Layers,
            YubeixIcons.Heavy.Link,
            YubeixIcons.Heavy.ListView,
            YubeixIcons.Heavy.Location,
            YubeixIcons.Heavy.Lock,
            YubeixIcons.Heavy.MapAlbum,
            YubeixIcons.Heavy.Merge,
            YubeixIcons.Heavy.Messages,
            YubeixIcons.Heavy.Mic,
            YubeixIcons.Heavy.MicSlash,
            YubeixIcons.Heavy.MindMap,
            YubeixIcons.Heavy.Months,
            YubeixIcons.Heavy.More,
            YubeixIcons.Heavy.MoreCircle,
            YubeixIcons.Heavy.MoveFile,
            YubeixIcons.Heavy.Music,
            YubeixIcons.Heavy.Notes,
            YubeixIcons.Heavy.NotesFill,
            YubeixIcons.Heavy.Ok,
            YubeixIcons.Heavy.Paste,
            YubeixIcons.Heavy.Pause,
            YubeixIcons.Heavy.Phone,
            YubeixIcons.Heavy.Photos,
            YubeixIcons.Heavy.Pin,
            YubeixIcons.Heavy.Play,
            YubeixIcons.Heavy.Playlist,
            YubeixIcons.Heavy.Promotions,
            YubeixIcons.Heavy.Recent,
            YubeixIcons.Heavy.Recording,
            YubeixIcons.Heavy.RecordingTape,
            YubeixIcons.Heavy.Redo,
            YubeixIcons.Heavy.Refresh,
            YubeixIcons.Heavy.Remove,
            YubeixIcons.Heavy.RemoveContact,
            YubeixIcons.Heavy.Rename,
            YubeixIcons.Heavy.Replace,
            YubeixIcons.Heavy.Reply,
            YubeixIcons.Heavy.ReplyAll,
            YubeixIcons.Heavy.Report,
            YubeixIcons.Heavy.Reset,
            YubeixIcons.Heavy.RotateLeft,
            YubeixIcons.Heavy.Scan,
            YubeixIcons.Heavy.ScreenCapture,
            YubeixIcons.Heavy.ScreenMirroring,
            YubeixIcons.Heavy.Search,
            YubeixIcons.Heavy.SearchDevice,
            YubeixIcons.Heavy.SelectAll,
            YubeixIcons.Heavy.Send,
            YubeixIcons.Heavy.Settings,
            YubeixIcons.Heavy.Share,
            YubeixIcons.Heavy.Show,
            YubeixIcons.Heavy.Sidebar,
            YubeixIcons.Heavy.Sort,
            YubeixIcons.Heavy.Stopwatch,
            YubeixIcons.Heavy.Store,
            YubeixIcons.Heavy.Tasks,
            YubeixIcons.Heavy.Th1,
            YubeixIcons.Heavy.Th10,
            YubeixIcons.Heavy.Th11,
            YubeixIcons.Heavy.Th12,
            YubeixIcons.Heavy.Th13,
            YubeixIcons.Heavy.Th14,
            YubeixIcons.Heavy.Th15,
            YubeixIcons.Heavy.Th16,
            YubeixIcons.Heavy.Th17,
            YubeixIcons.Heavy.Th18,
            YubeixIcons.Heavy.Th19,
            YubeixIcons.Heavy.Th2,
            YubeixIcons.Heavy.Th20,
            YubeixIcons.Heavy.Th21,
            YubeixIcons.Heavy.Th22,
            YubeixIcons.Heavy.Th23,
            YubeixIcons.Heavy.Th24,
            YubeixIcons.Heavy.Th25,
            YubeixIcons.Heavy.Th26,
            YubeixIcons.Heavy.Th27,
            YubeixIcons.Heavy.Th28,
            YubeixIcons.Heavy.Th29,
            YubeixIcons.Heavy.Th3,
            YubeixIcons.Heavy.Th30,
            YubeixIcons.Heavy.Th31,
            YubeixIcons.Heavy.Th4,
            YubeixIcons.Heavy.Th5,
            YubeixIcons.Heavy.Th6,
            YubeixIcons.Heavy.Th7,
            YubeixIcons.Heavy.Th8,
            YubeixIcons.Heavy.Th9,
            YubeixIcons.Heavy.Theme,
            YubeixIcons.Heavy.Timer,
            YubeixIcons.Heavy.TopDownloads,
            YubeixIcons.Heavy.Translate,
            YubeixIcons.Heavy.Trim,
            YubeixIcons.Heavy.Tune,
            YubeixIcons.Heavy.Undo,
            YubeixIcons.Heavy.Unlock,
            YubeixIcons.Heavy.Unpin,
            YubeixIcons.Heavy.Update,
            YubeixIcons.Heavy.UploadCloud,
            YubeixIcons.Heavy.VerticalSplit,
            YubeixIcons.Heavy.VolumeOff,
            YubeixIcons.Heavy.VolumeUp,
            YubeixIcons.Heavy.Weeks,
            YubeixIcons.Heavy.WorldClock,
            YubeixIcons.Heavy.Years,
            YubeixIcons.Heavy.ZoomOut,
        ),
    )
