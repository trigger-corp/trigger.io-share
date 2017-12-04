#import "share_API.h"

@implementation share_API

static NSDictionary* ActivityTypes = nil;
static dispatch_once_t onceToken;

+ (void)item:(ForgeTask *)task content:(NSDictionary *)content exclusions:(NSArray *)exclusions {
    // Initialize Target Mappings
    dispatch_once(&onceToken, ^{
        ActivityTypes = @{
            @"PostToFacebook": UIActivityTypePostToFacebook,
            @"PostToTwitter": UIActivityTypePostToTwitter,
            @"PostToWeibo": UIActivityTypePostToWeibo,
            @"Message": UIActivityTypeMessage,
            @"Mail": UIActivityTypeMail,
            @"Print": UIActivityTypePrint,
            @"CopyToPasteboard": UIActivityTypeCopyToPasteboard,
            @"AssignToContact": UIActivityTypeAssignToContact,
            @"SaveToCameraRoll": UIActivityTypeSaveToCameraRoll,
            @"AddToReadingList": UIActivityTypeAddToReadingList,
            @"PostToFlickr": UIActivityTypePostToFlickr,
            @"PostToVimeo": UIActivityTypePostToVimeo,
            @"PostToTencentWeibo": UIActivityTypePostToTencentWeibo,
            @"AirDrop": UIActivityTypeAirDrop,
            @"OpenInIBooks": UIActivityTypeOpenInIBooks
        };
    });

    // parse arguments
    NSMutableArray *share = [NSMutableArray new];
    if ([content objectForKey:@"text"] != nil) {
        [share addObject:[content objectForKey:@"text"]];
    }
    if ([content objectForKey:@"url"] != nil) {
        [share addObject:[NSURL URLWithString:[content objectForKey:@"url"]]];
    }
    if ([content objectForKey:@"image"]) {
        [share addObject:[UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:[content objectForKey:@"image"]]]]];
    }

    // create activity view controller
    UIActivityViewController *activity = [[UIActivityViewController alloc] initWithActivityItems:share applicationActivities:Nil];
    if ([content objectForKey:@"subject"] != nil) {
        [activity setValue:[content objectForKey:@"subject"] forKey:@"subject"];
    }

    // add exclusions
    NSMutableArray *excludedActivityTypes = [[NSMutableArray alloc] init];
    for (NSString *activity in exclusions) {
        if ([ActivityTypes objectForKey:activity] != nil) {
            [excludedActivityTypes addObject:[ActivityTypes objectForKey:activity]];
        }
    }
    activity.excludedActivityTypes = excludedActivityTypes;

    // Shamelessly borrowed from: https://github.com/leecrossley/cordova-plugin-social-message/issues/31
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0) {
        UIButton* invisibleShareButton = [UIButton buttonWithType:UIButtonTypeCustom];
        invisibleShareButton.bounds = CGRectMake(0 ,0, 0, 0);
        activity.popoverPresentationController.sourceView = invisibleShareButton;
    }

    [[[ForgeApp sharedApp] viewController] presentViewController:activity animated:YES completion:^{
        [task success:nil];
    }];
}


@end
