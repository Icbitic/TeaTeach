import { router } from '@kit.ArkUI';
import { TeacherMain } from './TeacherMain';

@Entry
@Component
struct TeacherPortal {
  build() {
    Column() {
      TeacherMain();
    }
  }
}